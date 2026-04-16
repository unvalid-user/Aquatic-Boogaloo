package com.example.aquaticboogaloo.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.Assert;

import java.util.*;

public class OAuth2UserPrincipal implements OAuth2User, CurrentUserView {
    private final Long userId;
    private final Set<GrantedAuthority> authorities;
    private final Map<String, Object> attributes;
    private final String nameAttributeKey;

    public OAuth2UserPrincipal(
            Long userId,
            Collection<? extends GrantedAuthority> authorities,
            Map<String, Object> attributes,
            String nameAttributeKey
    ) {
        Assert.notNull(userId,"User id cannot be null");
        this.userId = userId;
        this.authorities = Set.copyOf(authorities);
        this.attributes = Collections.unmodifiableMap(new HashMap(attributes));
        this.nameAttributeKey = nameAttributeKey;
    }

    @Override
    public Long getUserId() {
        return userId;
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
        Object value = attributes.get(nameAttributeKey);
        return value == null ? String.valueOf(userId) : value.toString();
    }
}
