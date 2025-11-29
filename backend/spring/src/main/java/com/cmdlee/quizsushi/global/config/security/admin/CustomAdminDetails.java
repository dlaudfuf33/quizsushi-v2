package com.cmdlee.quizsushi.global.config.security.admin;

import com.cmdlee.quizsushi.admin.domain.model.AdminMember;
import com.cmdlee.quizsushi.admin.domain.model.enums.AdminRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@Getter
public class CustomAdminDetails implements UserDetails, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final Long id;
    private final String username;
    private final String password;
    private final AdminRole role;

    public CustomAdminDetails(AdminMember admin) {
        this.id = admin.getId();
        this.username = admin.getUsername();
        this.password = admin.getPassword();
        this.role = admin.getRole();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + this.role.name());
        return List.of(authority);
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}