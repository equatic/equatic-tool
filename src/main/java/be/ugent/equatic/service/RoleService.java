package be.ugent.equatic.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import be.ugent.equatic.domain.Role;
import be.ugent.equatic.domain.RoleRepository;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Transactional
    public void save(Role role) {
        roleRepository.save(role);
    }

    public void deleteAll() {
        roleRepository.deleteAll();
    }
}
