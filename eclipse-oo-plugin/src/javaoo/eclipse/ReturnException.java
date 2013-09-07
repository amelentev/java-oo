package javaoo.eclipse;

@SuppressWarnings("serial")
public class ReturnException extends RuntimeException {
	private Object ret;
	public ReturnException(Object ret) {
		super();
		this.ret = ret;
	}
	public Object getReturn() {
		return ret;
	}
}