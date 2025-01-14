package org.domainmodule.agent.repository;

import org.domainmodule.agent.domain.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRepository extends JpaRepository<Agent, Long> {
}
