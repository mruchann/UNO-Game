package tr.edu.metu.ceng.uno.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Override
    public String toString() {
        return "ForgotPasswordRequest{" +
                "email='" + email +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForgotPasswordRequest forgotPasswordRequest = (ForgotPasswordRequest) o;
        return Objects.equals(email, forgotPasswordRequest.getEmail());


    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}
