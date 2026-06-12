package com.gomes.photographer_manager.security.oauth2;

import com.gomes.photographer_manager.domain.usuario.User;
import com.gomes.photographer_manager.domain.usuario.UserRepository;
import com.gomes.photographer_manager.enums.ProfileEnum;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createUser(email, name));

        return new OAuth2UserPrincipal(user, oAuth2User.getAttributes());
    }

    private User createUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setProfile(ProfileEnum.CLIENT);
        user.setActive(true);
        return userRepository.save(user);
    }
}
