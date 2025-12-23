package Report3;

public final class ErrorManagement {

	private ErrorManagement() {
	}

	public enum ErrorCode {
		ACCOUNTS_NOT_READY("E001", "계좌 목록이 초기화되지 않았습니다."),

		ACCOUNT_CODE_REQUIRED("E002", "계좌번호를 입력하세요."),
		INVALID_ACCOUNT_CODE("E003", "계좌번호 형식이 올바르지 않습니다. 예: 1234-1234"),
		ACCOUNT_ALREADY_EXISTS("E004", "이미 존재하는 계좌번호입니다."),
		ACCOUNT_NOT_FOUND("E005", "존재하지 않는 계좌번호입니다."),
		SAME_ACCOUNT_TRANSFER("E006", "동일 계좌로는 이체할 수 없습니다."),

		OWNER_REQUIRED("E007", "소유자 이름을 입력하세요."),
		INVALID_OWNER("E008", "소유자 이름은 한글 또는 영문만 가능합니다."),
		INVALID_PASSWORD("E009", "비밀번호는 최소 4자리 이상이어야 합니다."),
		PASSWORD_REQUIRED("E010", "비밀번호를 입력하세요."),
		PASSWORD_MISMATCH("E011", "비밀번호가 올바르지 않습니다."),

		AMOUNT_REQUIRED("E012", "금액을 입력하세요."),
		BALANCE_REQUIRED("E013", "잔액을 입력하세요."),
		INVALID_NON_NEGATIVE_INT("E014", "0 이상의 숫자여야 합니다."),
		INVALID_POSITIVE_INT("E015", "1 이상의 숫자여야 합니다."),
		INSUFFICIENT_BALANCE("E016", "잔액이 부족합니다."),

		IO_FAILURE("E017", "파일 저장/처리 중 오류가 발생했습니다."),
		UNKNOWN_ERROR("E018", "알 수 없는 오류가 발생했습니다.");

		private final String errorNumber;
		private final String defaultMessage;

		ErrorCode(String errorNumber, String defaultMessage) {
			this.errorNumber = errorNumber;
			this.defaultMessage = defaultMessage;
		}

		public String code() {
			return errorNumber;
		}

		public String message() {
			return defaultMessage;
		}

		public String formatted() {
			return errorNumber + " - " + defaultMessage;
		}
	}

	public static final class ValidationException extends RuntimeException {
		private final ErrorCode code;

		public ValidationException(ErrorCode code) {
			super(code.message());
			this.code = code;
		}

		public ValidationException(ErrorCode code, String message) {
			super(message);
			this.code = code;
		}

		public ErrorCode getCode() {
			return code;
		}
	}

	private static final String ACC_REGEX = "^\\d{4}-\\d{4}$";
	private static final String OWNER_REGEX = "^[가-힣a-zA-Z]+$";

	public static String trimToEmpty(String s) {
		return (s == null) ? "" : s.trim();
	}

	public static void requireAccountsReady(java.util.Map<?, ?> accounts) {
		if (accounts == null) throw new ValidationException(ErrorCode.ACCOUNTS_NOT_READY);
	}

	public static void validateAccountCode(String code) {
		String c = trimToEmpty(code);
		if (c.isEmpty()) throw new ValidationException(ErrorCode.ACCOUNT_CODE_REQUIRED);
		if (!java.util.regex.Pattern.matches(ACC_REGEX, c)) {
			throw new ValidationException(ErrorCode.INVALID_ACCOUNT_CODE);
		}
	}

	public static void validateOwner(String owner) {
		String o = trimToEmpty(owner);
		if (o.isEmpty()) throw new ValidationException(ErrorCode.OWNER_REQUIRED);
		if (!java.util.regex.Pattern.matches(OWNER_REGEX, o)) {
			throw new ValidationException(ErrorCode.INVALID_OWNER);
		}
	}

	public static void validatePasswordMinLength(String password) {
		if (password == null || password.length() < 4) {
			throw new ValidationException(ErrorCode.INVALID_PASSWORD);
		}
	}

	public static void requirePasswordProvided(String password) {
		if (password == null || password.trim().isEmpty()) {
			throw new ValidationException(ErrorCode.PASSWORD_REQUIRED);
		}
	}

	public static int parseNonNegativeInt(String label, String text) {
		String t = trimToEmpty(text);
		if (t.isEmpty()) {
			throw new ValidationException(ErrorCode.BALANCE_REQUIRED, label + "을(를) 입력하세요.");
		}
		try {
			int value = Integer.parseInt(t);
			if (value < 0) throw new ValidationException(ErrorCode.INVALID_NON_NEGATIVE_INT, label + "은(는) " + ErrorCode.INVALID_NON_NEGATIVE_INT.message());
			return value;
		} catch (NumberFormatException e) {
			throw new ValidationException(ErrorCode.INVALID_NON_NEGATIVE_INT, label + "은(는) " + ErrorCode.INVALID_NON_NEGATIVE_INT.message());
		}
	}

	public static int parsePositiveInt(String label, String text) {
		String t = trimToEmpty(text);
		if (t.isEmpty()) {
			throw new ValidationException(ErrorCode.AMOUNT_REQUIRED, label + "을(를) 입력하세요.");
		}
		try {
			int value = Integer.parseInt(t);
			if (value <= 0) throw new ValidationException(ErrorCode.INVALID_POSITIVE_INT, label + "은(는) " + ErrorCode.INVALID_POSITIVE_INT.message());
			return value;
		} catch (NumberFormatException e) {
			throw new ValidationException(ErrorCode.INVALID_POSITIVE_INT, label + "은(는) " + ErrorCode.INVALID_POSITIVE_INT.message());
		}
	}

	public static void requireAccountNotExists(java.util.Map<String, AccountManager.Account> accounts, String accCode) {
		if (accounts.containsKey(accCode)) throw new ValidationException(ErrorCode.ACCOUNT_ALREADY_EXISTS);
	}

	public static AccountManager.Account requireAccountExists(java.util.Map<String, AccountManager.Account> accounts, String accCode) {
		AccountManager.Account acc = accounts.get(accCode);
		if (acc == null) throw new ValidationException(ErrorCode.ACCOUNT_NOT_FOUND);
		return acc;
	}

	public static void requireDifferentAccounts(String from, String to) {
		if (from != null && from.equals(to)) throw new ValidationException(ErrorCode.SAME_ACCOUNT_TRANSFER);
	}

	public static void requirePasswordMatches(AccountManager.Account account, String password) {
		if (account == null) return;
		if (!account.verifyPassword(password)) throw new ValidationException(ErrorCode.PASSWORD_MISMATCH);
	}

	public static void requireSufficientBalance(AccountManager.Account account, int amount) {
		if (account == null) return;
		if (account.getBal() < amount) throw new ValidationException(ErrorCode.INSUFFICIENT_BALANCE);
	}
}
