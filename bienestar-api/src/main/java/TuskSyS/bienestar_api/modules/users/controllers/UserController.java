package TuskSyS.bienestar_api.modules.users.controllers;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import TuskSyS.bienestar_api.modules.users.dtos.LeaderboardDTO;
import TuskSyS.bienestar_api.modules.users.dtos.UserProfileRequest;
import TuskSyS.bienestar_api.modules.users.entities.User;
import TuskSyS.bienestar_api.modules.users.repositories.UserRepository;
import TuskSyS.bienestar_api.modules.users.services.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/profile/{userId}")
    public ResponseEntity<User> getProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<User> updateProfile(@PathVariable UUID userId, @RequestBody UserProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    @GetMapping("/leaderboard/{userId}")
    public ResponseEntity<?> getLeaderboard(@PathVariable UUID userId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<User> topUsers;
        if (currentUser.getCompany() != null) {
            topUsers = userRepository.findTop10ByCompanyOrderByCoinsDesc(currentUser.getCompany());
        } else {
            topUsers = userRepository.findTop10ByCompanyIsNullOrderByCoinsDesc();
        }
        
        List<LeaderboardDTO> leaderboard = topUsers.stream()
                .map(u -> new LeaderboardDTO(
                u.getFullName(),
                u.getCoins() != null ? u.getCoins() : 0,
                u.getCurrentStreak() != null ? u.getCurrentStreak() : 0,
                u.getProfilePictureUrl(),
                calculateRank(u.getCoins()) 
            ))
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(leaderboard);
    }

    private String calculateRank(Integer coins) {
        if (coins == null || coins < 100) return "Novato";
        if (coins < 500) return "Bronce";
        if (coins < 1500) return "Plata";
        if (coins < 3000) return "Oro";
        return "Leyenda 🔥";
    }

    @GetMapping("/{userId}/streak-status")
    public ResponseEntity<?> getStreakStatus(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getStreakStatus(userId));
    }
}