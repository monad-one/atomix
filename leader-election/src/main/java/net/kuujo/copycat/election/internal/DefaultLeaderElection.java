package net.kuujo.copycat.election.internal;

import net.kuujo.copycat.Coordinator;
import net.kuujo.copycat.cluster.Member;
import net.kuujo.copycat.election.ElectionResult;
import net.kuujo.copycat.election.LeaderElection;
import net.kuujo.copycat.internal.AbstractResource;
import net.kuujo.copycat.log.InMemoryLog;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultLeaderElection extends AbstractResource implements LeaderElection {
  private Consumer<Member> handler;
  private final Consumer<ElectionResult> electionHandler = result -> {
    if (handler != null) {
      handler.accept(result.winner());
    }
  };

  public DefaultLeaderElection(String name, Coordinator coordinator) {
    super(name, coordinator, resource -> new InMemoryLog());
  }

  @Override
  public LeaderElection handler(Consumer<Member> handler) {
    this.handler = handler;
    return this;
  }

  @Override
  public CompletableFuture<Void> open() {
    return super.open().thenAccept(result -> {
      cluster.election().handler(electionHandler);
    });
  }

  @Override
  public CompletableFuture<Void> close() {
    cluster.election().handler(null);
    return super.close();
  }

}
