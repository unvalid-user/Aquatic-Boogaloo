package com.example.aquaticboogaloo.security;

import com.example.aquaticboogaloo.entity.User;
import com.example.aquaticboogaloo.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DiscordOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final UserRepository userRepository;
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    // https://cdn.discordapp.com/avatars/1077633727150182402/3e44e4c5439cc30339d3391b936b7070.png
    private static final String DISCORD_AVATAR_URL = "https://cdn.discordapp.com/avatars/%s/%s.png";

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = delegate.loadUser(userRequest);
        Map<String, Object> attributes = oauth2User.getAttributes();

        String discordId = (String) attributes.get("id");
        if (discordId == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_user_info"),
                    "Discord did not return user id"
            );
        }

        User user = userRepository.findByDiscordUserId(discordId)
                .orElseGet(() -> new User(discordId));

        user.setUsername((String) attributes.get("username"));
        user.setAvatarUrl(DISCORD_AVATAR_URL.formatted(discordId, attributes.get("avatar")));

        user = userRepository.save(user);

        Set<GrantedAuthority> authorities = new HashSet<>(oauth2User.getAuthorities());
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        return new OAuth2UserPrincipal(user.getId(), authorities, attributes, "id");
    }
}
