package solutions.oneguard.msa.front.controller.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WebSocketMessage {
    @NotNull
    @NotBlank
    private String type;

    @NotNull
    private Object payload;

    @Builder.Default
    private Date occurredAt = new Date();

    private Object reference;
}
