package com.example.auths3.security;

import com.example.auths3.model.User;
import com.example.auths3.repository.RepositoryUser;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final RepositoryUser repositoryUser;
    public CustomUserDetailsService(RepositoryUser userRepo) {
        this.repositoryUser = userRepo;
    }

    //Se utiliza el email como username
    @Override
    public UserDetails loadUserByUsername (String email) throws UsernameNotFoundException{
        User Ur= repositoryUser.findByEmail(email)
                .orElseThrow(()->new UsernameNotFoundException("Usuario no encontrado por email: "+ email));

        var authorities = Ur.getRoles().stream()
                .map(r-> new SimpleGrantedAuthority(r.getName()))
                .toList();

        return org.springframework.security.core.userdetails.User
                .withUsername(Ur.getEmail())
                .password(Ur.getPasswordHash())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}