package io.github.iamnicknack.pjs.ffm.context.method;

/**
 * A functional interface for calling native methods with variable arguments.
 */
@FunctionalInterface
public interface MethodCaller {
    Object call(Object... args);

    /**
     * An exception class for handling errors during method calls.
     */
    class MethodCallerException extends RuntimeException {
        private final String methodName;

        public MethodCallerException(String methodName, Throwable cause) {
            super("Error calling method: " + methodName, cause);
            this.methodName = methodName;
        }

        public String getMethodName() {
            return methodName;
        }
    }
}
