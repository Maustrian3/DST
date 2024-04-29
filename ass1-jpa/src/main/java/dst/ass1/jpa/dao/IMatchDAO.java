package dst.ass1.jpa.dao;

import dst.ass1.jpa.model.IMatch;

import java.util.List;

public interface IMatchDAO extends GenericDAO<IMatch> {

    List<IMatch> findByDriverAndStates(Long id, List<String> matched);
}
