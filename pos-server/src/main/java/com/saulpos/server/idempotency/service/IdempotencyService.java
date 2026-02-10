package com.saulpos.server.idempotency.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.idempotency.model.IdempotencyKeyEventEntity;
import com.saulpos.server.idempotency.repository.IdempotencyKeyEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyKeyEventRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional
    public <TRequest, TResponse> TResponse execute(
            String endpointKey,
            String idempotencyKey,
            TRequest request,
            Class<TResponse> responseType,
            Supplier<TResponse> action) {

        String normalizedKey = normalizeKey(idempotencyKey);
        String requestHash = hashRequest(request);

        IdempotencyKeyEventEntity existing = repository
                .findByEndpointKeyAndIdempotencyKeyForUpdate(endpointKey, normalizedKey)
                .orElse(null);

        if (existing != null) {
            return resolveExisting(existing, requestHash, responseType);
        }

        IdempotencyKeyEventEntity event = new IdempotencyKeyEventEntity();
        event.setEndpointKey(endpointKey);
        event.setIdempotencyKey(normalizedKey);
        event.setRequestHash(requestHash);
        event.setResponsePayload("PENDING");

        try {
            repository.saveAndFlush(event);
        } catch (DataIntegrityViolationException exception) {
            IdempotencyKeyEventEntity concurrentEvent = repository
                    .findByEndpointKeyAndIdempotencyKeyForUpdate(endpointKey, normalizedKey)
                    .orElseThrow(() -> new BaseException(
                            ErrorCode.CONFLICT,
                            "idempotency conflict for key: " + normalizedKey));
            return resolveExisting(concurrentEvent, requestHash, responseType);
        }

        TResponse response = action.get();
        event.setResponsePayload(serializeResponse(response));
        repository.save(event);
        return response;
    }

    private <TResponse> TResponse resolveExisting(IdempotencyKeyEventEntity existing,
                                                  String requestHash,
                                                  Class<TResponse> responseType) {
        if (!existing.getRequestHash().equals(requestHash)) {
            throw new BaseException(
                    ErrorCode.CONFLICT,
                    "idempotency key reuse with different payload: " + existing.getIdempotencyKey());
        }

        if ("PENDING".equals(existing.getResponsePayload())) {
            throw new BaseException(
                    ErrorCode.CONFLICT,
                    "idempotency key is already being processed: " + existing.getIdempotencyKey());
        }

        return deserializeResponse(existing.getResponsePayload(), responseType);
    }

    private String normalizeKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "Idempotency-Key header is required");
        }

        String normalized = idempotencyKey.trim();
        if (normalized.length() > 120) {
            throw new BaseException(ErrorCode.VALIDATION_ERROR, "Idempotency-Key must be 120 characters or less");
        }
        return normalized;
    }

    private <TRequest> String hashRequest(TRequest request) {
        try {
            ObjectMapper canonicalMapper = objectMapper.copy()
                    .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
                    .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

            String payload = canonicalMapper.writeValueAsString(request);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hashBytes.length * 2);
            for (byte hashByte : hashBytes) {
                hex.append(String.format("%02x", hashByte));
            }
            return hex.toString();
        } catch (JsonProcessingException | NoSuchAlgorithmException exception) {
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "failed to process idempotency request hash");
        }
    }

    private <TResponse> String serializeResponse(TResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException exception) {
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "failed to persist idempotency response");
        }
    }

    private <TResponse> TResponse deserializeResponse(String payload, Class<TResponse> responseType) {
        try {
            return objectMapper.readValue(payload, responseType);
        } catch (JsonProcessingException exception) {
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "failed to replay idempotency response");
        }
    }
}
