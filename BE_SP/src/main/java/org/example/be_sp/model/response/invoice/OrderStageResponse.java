package org.example.be_sp.model.response.invoice;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderStageResponse {

    private Integer statusId;
    private String code;
    private String name;
    private LocalDateTime timestamp;
    private boolean reached;
    private boolean completed;
    private boolean current;
}
