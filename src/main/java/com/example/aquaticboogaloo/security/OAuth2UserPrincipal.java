package com.example.aquaticboogaloo.security;

import com.example.aquaticboogaloo.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.Assert;

import java.util.*;

@Getter
public class OAuth2UserPrincipal implements OAuth2User, CurrentUserView {
    private final User user;
    private final Set<GrantedAuthority> authorities;
    private final Map<String, Object> attributes;
    private final String nameAttributeKey;

    public OAuth2UserPrincipal(
            User user,
            Collection<? extends GrantedAuthority> authorities,
            Map<String, Object> attributes,
            String nameAttributeKey
    ) {
        Assert.notNull(user,"User cannot be null");
        this.user = user;
        this.authorities = Set.copyOf(authorities);
        this.attributes = Collections.unmodifiableMap(new HashMap(attributes));
        this.nameAttributeKey = nameAttributeKey;
    }

    @Override
    public Long getUserId() {
        return user.getId();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return user.getUsername();
    }
}
