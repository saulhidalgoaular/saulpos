package com.saulpos.core.hardware.scale;

public interface ScaleAdapter {

    ScaleReadResult read(ScaleReadRequest request);
}
