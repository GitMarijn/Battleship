package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import sun.security.util.Password;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@SpringBootApplication
public class SalvoApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(PlayerRepository playerRepository, GameRepository gameRepository, GamePlayerRepository gamePlayerRepository, ShipRepository shipRepository, SalvoRepository salvoRepository, ScoreRepository scoreRepository) {
		return (args) -> {

			Player player1 = new Player("j.bauer@ctu.gov", "24");
			Player player2 = new Player("c.obrian@ctu.gov", "42");
			Player player3 = new Player("kim_bauer@gmail.com", "kb");
			Player player4 = new Player("t.almeida@ctu.gov", "mole");
			Player player5 = new Player("d.palmer@whitehouse.gov", "");


			playerRepository.save(player1);
			playerRepository.save(player2);
			playerRepository.save(player3);
			playerRepository.save(player4);
			playerRepository.save(player5);

			Date date1 = new Date();
			Date date2 = Date.from(date1.toInstant().plusSeconds(3600));
			Date date3 = Date.from(date2.toInstant().plusSeconds(3600));
			Date date4 = Date.from(date3.toInstant().plusSeconds(3600));
			Date date5 = Date.from(date4.toInstant().plusSeconds(3600));
			Date date6 = Date.from(date5.toInstant().plusSeconds(3600));

			Game game1 = new Game(date1);
			Game game2 = new Game(date2);
			Game game3 = new Game(date3);
			Game game4 = new Game(date4);
			Game game5 = new Game(date5);
			Game game6 = new Game(date6);

			gameRepository.save(game1);
			gameRepository.save(game2);
			gameRepository.save(game3);
			gameRepository.save(game4);
			gameRepository.save(game5);
			gameRepository.save(game6);

			Date joinDate1 = new Date();

			GamePlayer gamePlayer1 = new GamePlayer(game1, player1, joinDate1);
			GamePlayer gamePlayer2 = new GamePlayer(game1, player2, joinDate1);
			GamePlayer gamePlayer3 = new GamePlayer(game2, player1, joinDate1);
			GamePlayer gamePlayer4 = new GamePlayer(game2, player2, joinDate1);
			GamePlayer gamePlayer5 = new GamePlayer(game3, player2, joinDate1);
			GamePlayer gamePlayer6 = new GamePlayer(game3, player4, joinDate1);
			GamePlayer gamePlayer7 = new GamePlayer(game4, player1, joinDate1);
			GamePlayer gamePlayer8 = new GamePlayer(game4, player2, joinDate1);
			GamePlayer gamePlayer9 = new GamePlayer(game5, player4, joinDate1);
			GamePlayer gamePlayer10 = new GamePlayer(game5, player1, joinDate1);
			GamePlayer gamePlayer11 = new GamePlayer(game6, player5, joinDate1);

			gamePlayerRepository.save(gamePlayer1);
			gamePlayerRepository.save(gamePlayer2);
			gamePlayerRepository.save(gamePlayer3);
			gamePlayerRepository.save(gamePlayer4);
			gamePlayerRepository.save(gamePlayer5);
			gamePlayerRepository.save(gamePlayer6);
			gamePlayerRepository.save(gamePlayer7);
			gamePlayerRepository.save(gamePlayer8);
			gamePlayerRepository.save(gamePlayer9);
			gamePlayerRepository.save(gamePlayer10);
			gamePlayerRepository.save(gamePlayer11);


			List<String> location1 = Arrays.asList("B5","B6","B7", "B8", "B9");
			List<String> location2 = Arrays.asList("F3", "G3", "H3","I3");
			List<String> location3 = Arrays.asList("A1", "A2", "A3");
			List<String> location4 = Arrays.asList("B1", "B2", "B3");
			List<String> location5 = Arrays.asList("C1", "C2");

			Ship ship1 = new Ship("aircraft-carrier", gamePlayer1, location1);
			Ship ship2 = new Ship("battleship", gamePlayer1, location2);
			Ship ship3 = new Ship("submarine", gamePlayer1, location3);
			Ship ship4 = new Ship("destroyer", gamePlayer1, location4);
			Ship ship5 = new Ship("patrol-boat", gamePlayer1, location5);

			Ship ship11 = new Ship("aircraft-carrier", gamePlayer2, location1);
			Ship ship21 = new Ship("battleship", gamePlayer2, location2);
			Ship ship31 = new Ship("submarine", gamePlayer2, location3);
			Ship ship41 = new Ship("destroyer", gamePlayer2, location4);
			Ship ship51 = new Ship("patrol-boat", gamePlayer2, location5);

			Ship ship6 = new Ship("aircraft-carrier", gamePlayer3, location1);
			Ship ship7 = new Ship("battleship", gamePlayer3, location2);
			Ship ship8 = new Ship("submarine", gamePlayer3, location3);
			Ship ship9 = new Ship("destroyer", gamePlayer3, location4);
			Ship ship10 = new Ship("patrol-boat", gamePlayer3, location5);

			Ship ship61 = new Ship("aircraft-carrier", gamePlayer4, location1);
			Ship ship71 = new Ship("battleship", gamePlayer4, location2);
			Ship ship81 = new Ship("submarine", gamePlayer4, location3);
			Ship ship91 = new Ship("destroyer", gamePlayer4, location4);
			Ship ship101 = new Ship("patrol-boat", gamePlayer4, location5);

			shipRepository.save(ship1);
			shipRepository.save(ship2);
			shipRepository.save(ship3);
			shipRepository.save(ship4);
			shipRepository.save(ship5);

			shipRepository.save(ship11);
			shipRepository.save(ship21);
			shipRepository.save(ship31);
			shipRepository.save(ship41);
			shipRepository.save(ship51);

			shipRepository.save(ship6);
			shipRepository.save(ship7);
			shipRepository.save(ship8);
			shipRepository.save(ship9);
			shipRepository.save(ship10);

			shipRepository.save(ship61);
			shipRepository.save(ship71);
			shipRepository.save(ship81);
			shipRepository.save(ship91);
			shipRepository.save(ship101);

			List<String> salvoLocation1 = Arrays.asList("B5", "C5", "F1");
			List<String> salvoLocation2 = Arrays.asList("B4", "B5", "B6");
			List<String> salvoLocation3 = Arrays.asList("F2", "D5");
			List<String> salvoLocation4 = Arrays.asList("E1", "H3", "A2");

			Salvo salvo1 = new Salvo(1, gamePlayer1, location1);
			Salvo salvo2 = new Salvo(2, gamePlayer1, location2);
			Salvo salvo3 = new Salvo(3, gamePlayer1, location3);
			Salvo salvo4 = new Salvo(4, gamePlayer1, location4);


			Salvo salvo7 = new Salvo(1, gamePlayer2, location1);
			Salvo salvo5 = new Salvo(2, gamePlayer2, location2);
			Salvo salvo6 = new Salvo(3, gamePlayer2, location3);
			Salvo salvo8 = new Salvo(4, gamePlayer2, location4);

			Salvo salvo21 = new Salvo(1, gamePlayer3, location1);
			Salvo salvo22 = new Salvo(2, gamePlayer3, location2);
			Salvo salvo23 = new Salvo(3, gamePlayer3, location3);
			Salvo salvo24 = new Salvo(4, gamePlayer3, location4);


			Salvo salvo27 = new Salvo(1, gamePlayer4, location1);
			Salvo salvo25 = new Salvo(2, gamePlayer4, location2);
			Salvo salvo26 = new Salvo(3, gamePlayer4, location3);
			Salvo salvo28 = new Salvo(4, gamePlayer4, location4);

			salvoRepository.save(salvo1);
			salvoRepository.save(salvo2);
			salvoRepository.save(salvo3);
			salvoRepository.save(salvo4);
			salvoRepository.save(salvo5);
			salvoRepository.save(salvo6);
			salvoRepository.save(salvo7);
			salvoRepository.save(salvo8);
			salvoRepository.save(salvo21);
			salvoRepository.save(salvo22);
			salvoRepository.save(salvo23);
			salvoRepository.save(salvo24);
			salvoRepository.save(salvo25);
			salvoRepository.save(salvo26);
			salvoRepository.save(salvo27);
			salvoRepository.save(salvo28);

			Date finishDate1 = new Date();

			Score score1 = new Score(game1, player1, 1.0, finishDate1);
			Score score2 = new Score(game1, player2, 0.0, finishDate1);
			Score score3 = new Score(game2, player1, 0.5, finishDate1);
			Score score4 = new Score(game2, player2, 0.5, finishDate1);
			Score score5 = new Score(game3, player2, 1.0, finishDate1);
			Score score6 = new Score(game3, player4, 0.0, finishDate1);
			Score score7 = new Score(game4, player2, 0.5, finishDate1);
			Score score8 = new Score(game4, player1, 0.5, finishDate1);

//			scoreRepository.save(score1);
//			scoreRepository.save(score2);
//			scoreRepository.save(score3);
//			scoreRepository.save(score4);
//			scoreRepository.save(score5);
//			scoreRepository.save(score6);
//			scoreRepository.save(score7);
//			scoreRepository.save(score8);
		};
	}
}

@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

	@Autowired
	PlayerRepository playerRepository;

	@Override
	public void init(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userName-> {
			Player player = playerRepository.findByUserName(userName);
			if (player != null) {
				return new User(player.getUserName(), player.getPassword(),
						AuthorityUtils.createAuthorityList("USER"));
			} else {
				throw new UsernameNotFoundException("Unknown user: " + userName);
			}
		});
	}
}

@Configuration
@EnableWebSecurity
class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.antMatchers("/web/**").permitAll()
				.antMatchers("/api/login").permitAll()
				.antMatchers("/api/players").permitAll()
				.antMatchers("/api/games").permitAll()
				.antMatchers("/api/scoreboard").permitAll()
				.antMatchers("/favicon.ico").permitAll()
				.antMatchers("/rest/**").denyAll()
				.anyRequest().fullyAuthenticated()
				.and()
				.formLogin()
				.usernameParameter("userName")
				.passwordParameter("password")
				.loginPage("/api/login");

		http.logout().logoutUrl("/api/logout");

		// turn off checking for CSRF tokens
		http.csrf().disable();

		// if user is not authenticated, just send an authentication failure response
		http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		// if login is successful, just clear the flags asking for authentication
		http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));

		// if login fails, just send an authentication failure response
		http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		// if logout is successful, just send a success response
		http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
	}

	private void clearAuthenticationAttributes(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
		}
	}
}