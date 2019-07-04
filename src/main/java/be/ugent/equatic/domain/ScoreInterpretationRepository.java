package be.ugent.equatic.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScoreInterpretationRepository extends JpaRepository<ScoreInterpretation, Long> {

    List<ScoreInterpretation> findByInstitution(Institution institution);
}
