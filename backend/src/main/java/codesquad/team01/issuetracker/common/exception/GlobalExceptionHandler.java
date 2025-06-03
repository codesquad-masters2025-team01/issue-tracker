package codesquad.team01.issuetracker.common.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import codesquad.team01.issuetracker.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(InvalidParameterException.class)
	public ResponseEntity<ApiResponse<?>> handleInvalidParameterException(InvalidParameterException e) {
		log.error("InvalidParameterException: {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class) // @Valid에서 발생하는 에러
	public ResponseEntity<ApiResponse<?>> handleValidationExceptions(MethodArgumentNotValidException e) {
		String errorMessage = e.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(FieldError::getDefaultMessage)
			.collect(Collectors.joining(", "));

		log.error("Bean Validation error(@Valid): {}", errorMessage);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(errorMessage));
	}

	@ExceptionHandler(DuplicateLabelName.class)    // 레이블 생성 시 이미 생성된 레이블과 이름 중복
	public ResponseEntity<ApiResponse<?>> handleDuplicateLabel(DuplicateLabelName e) {
		String labelName = e.getLabelName();
		return ResponseEntity
			.badRequest()
			.body(ApiResponse.error("레이블 이름 '" + labelName + "'은(는) 이미 존재합니다."));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<?>> handleExceptions(Exception e) {
		log.error("Unhandled exception", e);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("서버 내부 오류가 발생했습니다."));
	}

	//아이디가 존재하지 않는 경우
	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ApiResponse<?>> handleUserNotFound(UserNotFoundException e) {
		log.error("UserNotFoundException : {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("존재하지 않는 ID 입니다"));
	}

	//패스워드가 틀린 경우
	@ExceptionHandler(InvalidPasswordException.class)
	public ResponseEntity<ApiResponse<?>> handleInvalidPassword(InvalidPasswordException e) {
		log.error("InvalidPasswordException : {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("비밀번호가 일치하지 않습니다"));
	}

}
