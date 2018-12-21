package com.lmx.pactdemoconsumer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PactEntity {
    private String methodDesc;
    private String path;
    private String upon;
    private String provider;
    private String consumer;
}
