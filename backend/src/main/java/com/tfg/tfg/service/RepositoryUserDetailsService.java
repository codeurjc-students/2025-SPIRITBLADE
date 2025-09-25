package com.tfg.tfg.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.tfg.tfg.repository.UserModelRepository;
import com.tfg.tfg.model.entity.UserModel;



@Service
public class RepositoryUserDetailsService implements UserDetailsService {

	@Autowired
	private UserModelRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		UserModel user = userRepository.findByName(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		List<GrantedAuthority> roles = new ArrayList<>();
		for (String role : user.getRols()) {
			roles.add(new SimpleGrantedAuthority("ROLE_" + role));
		}

		return new org.springframework.security.core.userdetails.User(user.getName(), 
				user.getEncodedPassword(), roles);

	}
}
