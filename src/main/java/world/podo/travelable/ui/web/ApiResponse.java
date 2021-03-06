package world.podo.travelable.ui.web;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;

@Data
public class ApiResponse<T> {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T data;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<ErrorResponse> errors;

    @JsonCreator
    private ApiResponse(T data, List<ErrorResponse> errors) {
        this.data = data;
        this.errors = errors;
    }

    public static ApiResponse empty() {
        return new ApiResponse<>(null, Collections.emptyList());
    }

    public static <T> ApiResponse<T> data(T data) {
        Assert.notNull(data, "'data' must not be null");
        return new ApiResponse<>(data, Collections.emptyList());
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        Assert.notNull(code, "'code' must not be null");
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setCode(code);
        errorResponse.setMessage(message);
        return new ApiResponse<>(
                null,
                Collections.singletonList(errorResponse)
        );
    }
}
