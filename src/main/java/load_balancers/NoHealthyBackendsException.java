package load_balancers;

public class NoHealthyBackendsException extends RuntimeException {

  public NoHealthyBackendsException() {
    super("No healthy backends are available");
  }
}
