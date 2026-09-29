package com.ridelink.account;

import com.ridelink.account.dto.AccountResponse;
import com.ridelink.account.dto.LoginRequest;
import com.ridelink.account.dto.ProfileUpdateRequest;
import com.ridelink.account.dto.RegisterRequest;
import com.ridelink.account.dto.RoleUpdateRequest;
import com.ridelink.account.dto.StatusUpdateRequest;
import com.ridelink.account.entity.Account;
import com.ridelink.account.enums.AccountStatus;
import com.ridelink.account.enums.Role;
import com.ridelink.account.exception.DuplicateResourceException;
import com.ridelink.account.exception.InvalidCredentialsException;
import com.ridelink.account.exception.ResourceNotFoundException;
import com.ridelink.account.repository.AccountRepository;
import com.ridelink.account.security.JwtService;
import com.ridelink.account.service.AccountService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AccountService accountService;

    private Account account;

    @BeforeEach
    void setUp() {

        account = new Account();

        account.setId(1L);
        account.setName("Test User");
        account.setEmail("test@ridelink.com");
        account.setPassword("encodedPassword");
        account.setRole(Role.PASSENGER);
        account.setStatus(AccountStatus.ACTIVE);
    }

   
    //  REGISTER - SUCCESS
    

    @Test
    void registerSuccess() {

        RegisterRequest request = new RegisterRequest();

        request.setName("Test User");
        request.setEmail("test@ridelink.com");
        request.setPassword("TestPass123");

        when(accountRepository.existsByEmail(request.getEmail()))
                .thenReturn(false);

        when(passwordEncoder.encode(request.getPassword()))
                .thenReturn("encodedPassword");

        when(accountRepository.save(any(Account.class)))
                .thenReturn(account);

        AccountResponse response =
                accountService.register(request);

        assertNotNull(response);
        assertEquals("Test User", response.getName());
        assertEquals("test@ridelink.com", response.getEmail());
        assertEquals(Role.PASSENGER, response.getRole());
        assertEquals(AccountStatus.ACTIVE, response.getStatus());

        verify(accountRepository).save(any(Account.class));
    }

    
     // REGISTER - DUPLICATE EMAIL
    
    @Test
    void registerDuplicateEmail() {

        RegisterRequest request = new RegisterRequest();

        request.setName("Duplicate User");
        request.setEmail("test@ridelink.com");
        request.setPassword("TestPass123");

        when(accountRepository.existsByEmail(request.getEmail()))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> accountService.register(request)
        );

        verify(accountRepository, never())
                .save(any(Account.class));
    }

    
    //  LOGIN - SUCCESS


    @Test
    void loginSuccess() {

        LoginRequest request = new LoginRequest();

        request.setEmail("test@ridelink.com");
        request.setPassword("TestPass123");

        when(accountRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(account));

        when(passwordEncoder.matches(
                request.getPassword(),
                account.getPassword()))
                .thenReturn(true);

        when(jwtService.generateToken(account.getEmail()))
                .thenReturn("test-jwt-token");

        String token = accountService.login(request);

        assertNotNull(token);
        assertEquals("test-jwt-token", token);

        verify(jwtService).generateToken(account.getEmail());
    }

    
    // LOGIN - WRONG PASSWORD
    

    @Test
    void loginWrongPassword() {

        LoginRequest request = new LoginRequest();

        request.setEmail("test@ridelink.com");
        request.setPassword("WrongPassword");

        when(accountRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(account));

        when(passwordEncoder.matches(
                request.getPassword(),
                account.getPassword()))
                .thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> accountService.login(request)
        );

        verify(jwtService, never())
                .generateToken(anyString());
    }

   
    //  LOGIN - ACCOUNT NOT FOUND
    

    @Test
    void loginAccountNotFound() {

        LoginRequest request = new LoginRequest();

        request.setEmail("unknown@ridelink.com");
        request.setPassword("TestPass123");

        when(accountRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> accountService.login(request)
        );
    }

   
    // LOGIN - INACTIVE ACCOUNT
    
    @Test
    void loginInactiveAccount() {

        account.setStatus(AccountStatus.SUSPENDED);

        LoginRequest request = new LoginRequest();

        request.setEmail("test@ridelink.com");
        request.setPassword("TestPass123");

        when(accountRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(account));

        when(passwordEncoder.matches(
                request.getPassword(),
                account.getPassword()))
                .thenReturn(true);

        assertThrows(
                InvalidCredentialsException.class,
                () -> accountService.login(request)
        );

        verify(jwtService, never())
                .generateToken(anyString());
    }

    
    //  GET PROFILE - SUCCESS
    

    @Test
    void getProfileSuccess() {

        when(accountRepository.findByEmail(account.getEmail()))
                .thenReturn(Optional.of(account));

        AccountResponse response =
                accountService.getProfile(account.getEmail());

        assertNotNull(response);
        assertEquals(account.getId(), response.getId());
        assertEquals(account.getName(), response.getName());
        assertEquals(account.getEmail(), response.getEmail());
        assertEquals(account.getRole(), response.getRole());
        assertEquals(account.getStatus(), response.getStatus());
    }

  
    // GET PROFILE - NOT FOUND
   

    @Test
    void getProfileNotFound() {

        when(accountRepository.findByEmail("unknown@ridelink.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> accountService.getProfile(
                        "unknown@ridelink.com")
        );
    }

    
    //  UPDATE PROFILE - SUCCESS
   

    @Test
    void updateProfileSuccess() {

        ProfileUpdateRequest request =
                new ProfileUpdateRequest();

        request.setName("Updated User");

        when(accountRepository.findByEmail(account.getEmail()))
                .thenReturn(Optional.of(account));

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        AccountResponse response =
                accountService.updateProfile(
                        account.getEmail(),
                        request
                );

        assertNotNull(response);
        assertEquals("Updated User", response.getName());

        verify(accountRepository).save(account);
    }

    
    // UPDATE ROLE - SUCCESS
    

    @Test
    void updateRoleSuccess() {

        RoleUpdateRequest request =
                new RoleUpdateRequest();

        request.setRole(Role.DRIVER);

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        AccountResponse response =
                accountService.updateRole(1L, request);

        assertNotNull(response);
        assertEquals(Role.DRIVER, response.getRole());

        verify(accountRepository).save(account);
    }

   
    //  UPDATE STATUS - SUCCESS
   

    @Test
    void updateStatusSuccess() {

        StatusUpdateRequest request =
                new StatusUpdateRequest();

        request.setStatus(AccountStatus.SUSPENDED);

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        AccountResponse response =
                accountService.updateStatus(1L, request);

        assertNotNull(response);
        assertEquals(
                AccountStatus.SUSPENDED,
                response.getStatus()
        );

        verify(accountRepository).save(account);
    }

    
    //  UPDATE ROLE - ACCOUNT NOT FOUND
    

    @Test
    void updateRoleAccountNotFound() {

        RoleUpdateRequest request =
                new RoleUpdateRequest();

        request.setRole(Role.DRIVER);

        when(accountRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> accountService.updateRole(999L, request)
        );
    }

    
    // UPDATE STATUS - ACCOUNT NOT FOUND
    

    @Test
    void updateStatusAccountNotFound() {

        StatusUpdateRequest request =
                new StatusUpdateRequest();

        request.setStatus(AccountStatus.SUSPENDED);

        when(accountRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> accountService.updateStatus(999L, request)
        );
    }
}