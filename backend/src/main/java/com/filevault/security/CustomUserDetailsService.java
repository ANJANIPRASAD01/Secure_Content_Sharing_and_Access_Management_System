package com.filevault.security;

import com.filevault.entity.Admin;
import com.filevault.entity.User;
import com.filevault.repository.AdminRepository;
import com.filevault.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Try to find admin first
        Optional<Admin> admin = adminRepository.findByEmail(email);
        if (admin.isPresent()) {
            Admin foundAdmin = admin.get();
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            
            return new org.springframework.security.core.userdetails.User(
                    foundAdmin.getEmail(),
                    foundAdmin.getPassword(),
                    foundAdmin.getIsActive(),
                    true,
                    true,
                    true,
                    authorities
            );
        }
        
        // Try to find user
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            User foundUser = user.get();
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            
            return new org.springframework.security.core.userdetails.User(
                    foundUser.getEmail(),
                    foundUser.getPassword() != null ? foundUser.getPassword() : "",
                    foundUser.getIsActive(),
                    true,
                    true,
                    true,
                    authorities
            );
        }
        
        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}
