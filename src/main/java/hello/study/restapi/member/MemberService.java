package hello.study.restapi.member;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MemberService implements UserDetailsService {

	@Autowired
	private MemberRepository memberRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Member member = memberRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException(username));
		return new User(member.getEmail(), member.getPassword(), authorities(member.getRoles()));
	}

	private Collection<? extends GrantedAuthority> authorities(Set<MemberRole> roles) {
		return roles.stream()
			.map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
			.collect(Collectors.toSet());
	}
}