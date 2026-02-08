package com.saulpos.server.shift.web;

import com.saulpos.api.shift.CashMovementRequest;
import com.saulpos.api.shift.CashMovementResponse;
import com.saulpos.api.shift.CashShiftCloseRequest;
import com.saulpos.api.shift.CashShiftOpenRequest;
import com.saulpos.api.shift.CashShiftResponse;
import com.saulpos.server.shift.service.ShiftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;

    @PostMapping("/open")
    @ResponseStatus(HttpStatus.CREATED)
    public CashShiftResponse openShift(@Valid @RequestBody CashShiftOpenRequest request) {
        return shiftService.openShift(request);
    }

    @PostMapping("/{id}/cash-movements")
    @ResponseStatus(HttpStatus.CREATED)
    public CashMovementResponse addCashMovement(@PathVariable("id") Long id,
                                                @Valid @RequestBody CashMovementRequest request) {
        return shiftService.addCashMovement(id, request);
    }

    @PostMapping("/{id}/close")
    public CashShiftResponse closeShift(@PathVariable("id") Long id,
                                        @Valid @RequestBody CashShiftCloseRequest request) {
        return shiftService.closeShift(id, request);
    }

    @GetMapping("/{id}")
    public CashShiftResponse getShift(@PathVariable("id") Long id) {
        return shiftService.getShift(id);
    }
}
