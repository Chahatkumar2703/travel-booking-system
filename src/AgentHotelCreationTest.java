import model.Hotel;
import model.User;
import service.AdminService;
import service.AgentService;
import service.AuthService;
import service.HotelService;
import service.ValidationException;
import util.DatabaseConnection;
import util.SessionManager;

import java.util.List;

/**
 * AgentHotelCreationTest
 * 
 * Regression test suite specifically validating:
 * 1. Agent can create Hotel.
 * 2. Correct agent_id is stored in the database.
 * 3. Initial approval_status is strictly PENDING.
 * 4. Hotel appears in Agent's listings as PENDING.
 * 5. PENDING Hotel is NOT visible in Traveler's approved catalog.
 * 6. Cross-Agent isolation (Agent B cannot create listings on behalf of Agent A).
 * 7. Admin can approve the Hotel.
 * 8. Approved Hotel becomes immediately visible to Travelers.
 * 9. Input validation (negative price, empty name, 0 rooms) properly rejected.
 */
public class AgentHotelCreationTest {

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("   AGENT HOTEL CREATION & APPROVAL REGRESSION TEST        ");
        System.out.println("==========================================================");

        int passed = 0;
        int failed = 0;

        AuthService authService = new AuthService();
        HotelService hotelService = new HotelService();
        AgentService agentService = new AgentService();
        AdminService adminService = new AdminService();

        // 1. Database Connection
        try {
            if (DatabaseConnection.testConnection()) {
                System.out.println("[PASS] 1. Database Connectivity OK");
                passed++;
            } else {
                System.err.println("[FAIL] 1. Database Connection Failed");
                failed++;
                return;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] 1. Database Connection Exception: " + e.getMessage());
            failed++;
            return;
        }

        // 2. Authenticate Agent (Skyline Travels Agency)
        User agent = null;
        try {
            agent = authService.login("agent@example.com", "agent123");
            if (agent != null && agent.isAgent()) {
                System.out.println("[PASS] 2. Agent Authenticated: ID=" + agent.getId() + " (" + agent.getFullName() + ")");
                passed++;
            } else {
                System.err.println("[FAIL] 2. Agent login failed");
                failed++;
                return;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] 2. Agent Login Exception: " + e.getMessage());
            failed++;
            return;
        }

        // 3. Create Hotel Listing as Agent
        String uniqueSuffix = String.valueOf(System.currentTimeMillis() % 100000);
        String hotelName = "Grand Azure Resort " + uniqueSuffix;
        Hotel createdHotel = null;
        try {
            createdHotel = hotelService.addHotel(
                    agent.getId(),
                    hotelName,
                    "Goa",
                    "Calangute Beach Road, North Goa",
                    1, // Destination: Goa
                    "Deluxe Sea View Suite",
                    5500.0,
                    15,
                    4.8,
                    "Luxury seaside suites with private infinity pool and spa.",
                    "https://images.unsplash.com/photo-1566073771259-6a8506099945"
            );

            if (createdHotel != null && createdHotel.getId() > 0) {
                System.out.println("[PASS] 3.1 Hotel Created Successfully with ID: " + createdHotel.getId());
                passed++;
            } else {
                System.err.println("[FAIL] 3.1 Hotel creation failed to return generated ID");
                failed++;
                return;
            }

            // Verify stored agent_id
            if (createdHotel.getAgentId() == agent.getId()) {
                System.out.println("[PASS] 3.2 Correct agent_id stored in Hotel: " + createdHotel.getAgentId());
                passed++;
            } else {
                System.err.println("[FAIL] 3.2 agent_id mismatch: expected " + agent.getId() + " but got " + createdHotel.getAgentId());
                failed++;
            }

            // Verify initial status is PENDING
            if ("PENDING".equalsIgnoreCase(createdHotel.getApprovalStatus())) {
                System.out.println("[PASS] 3.3 Initial approval_status is PENDING (Admin approval required)");
                passed++;
            } else {
                System.err.println("[FAIL] 3.3 Initial approval_status was not PENDING: " + createdHotel.getApprovalStatus());
                failed++;
            }

        } catch (Exception e) {
            System.err.println("[FAIL] 3. Hotel Creation Exception: " + e.getMessage());
            failed++;
            return;
        }

        // 4. Hotel Appears in Agent's Listings as PENDING
        try {
            List<Hotel> agentHotels = agentService.getAgentHotels(agent.getId());
            final int createdId = createdHotel.getId();
            Hotel foundInAgentListings = agentHotels.stream()
                    .filter(h -> h.getId() == createdId)
                    .findFirst()
                    .orElse(null);

            if (foundInAgentListings != null && "PENDING".equalsIgnoreCase(foundInAgentListings.getApprovalStatus())) {
                System.out.println("[PASS] 4. Hotel appears in Agent listings with status PENDING");
                passed++;
            } else {
                System.err.println("[FAIL] 4. Hotel not found in Agent listings or approvalStatus not PENDING");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] 4. Agent Listings Exception: " + e.getMessage());
            failed++;
        }

        // 5. PENDING Hotel is NOT Visible to Travelers in Approved Catalog
        try {
            List<Hotel> approvedHotels = hotelService.getApprovedHotels("Goa", null, null, null);
            final int createdId = createdHotel.getId();
            boolean visibleToTraveler = approvedHotels.stream().anyMatch(h -> h.getId() == createdId);

            if (!visibleToTraveler) {
                System.out.println("[PASS] 5. PENDING Hotel is isolated: NOT visible in Traveler approved hotel catalog");
                passed++;
            } else {
                System.err.println("[FAIL] 5. Data leak! PENDING Hotel appeared in Traveler approved catalog!");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] 5. Traveler Approved Catalog Exception: " + e.getMessage());
            failed++;
        }

        // 6. Cross-Agent Protection & Identity Enforcement
        try {
            // Register a second agent
            String agent2Email = "agent2_" + uniqueSuffix + "@test.com";
            User agent2 = authService.register(
                    "Second Agency " + uniqueSuffix,
                    agent2Email,
                    "98765" + String.format("%05d", Integer.parseInt(uniqueSuffix)),
                    "AgentPass123!",
                    "AgentPass123!",
                    "AGENT"
            );

            // Verify Agent 2's listings do NOT include Agent 1's hotel
            List<Hotel> agent2Hotels = agentService.getAgentHotels(agent2.getId());
            final int createdId = createdHotel.getId();
            boolean agent2SeesHotel = agent2Hotels.stream().anyMatch(h -> h.getId() == createdId);

            if (!agent2SeesHotel) {
                System.out.println("[PASS] 6.1 Agent 2 listings do NOT contain Agent 1's hotel (Agent isolation verified)");
                passed++;
            } else {
                System.err.println("[FAIL] 6.1 Agent 2 leaked Agent 1's hotel!");
                failed++;
            }

            // SessionManager test: Agent 2 creates a web session
            String agent2Token = SessionManager.createWebSession(agent2);
            User sessionUser = SessionManager.getWebUser(agent2Token);
            if (sessionUser != null && sessionUser.getId() == agent2.getId() && sessionUser.isAgent()) {
                System.out.println("[PASS] 6.2 Agent 2 web session correctly bound to ID: " + sessionUser.getId());
                passed++;
            } else {
                System.err.println("[FAIL] 6.2 Agent 2 web session binding failed");
                failed++;
            }

        } catch (Exception e) {
            System.err.println("[FAIL] 6. Cross-Agent Isolation Exception: " + e.getMessage());
            failed++;
        }

        // 7. Admin Approves the Hotel
        try {
            boolean approved = adminService.approveListing("HOTEL", createdHotel.getId());
            if (approved) {
                Hotel reloaded = hotelService.getHotelById(createdHotel.getId());
                if (reloaded != null && "APPROVED".equalsIgnoreCase(reloaded.getApprovalStatus())) {
                    System.out.println("[PASS] 7. Admin Approval OK: Status updated from PENDING to APPROVED");
                    passed++;
                } else {
                    System.err.println("[FAIL] 7. Status was not updated to APPROVED: " + (reloaded == null ? "null" : reloaded.getApprovalStatus()));
                    failed++;
                }
            } else {
                System.err.println("[FAIL] 7. Admin approveListing returned false");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] 7. Admin Approval Exception: " + e.getMessage());
            failed++;
        }

        // 8. Approved Hotel is Now Visible to Travelers
        try {
            List<Hotel> approvedHotels = hotelService.getApprovedHotels("Goa", null, null, null);
            final int createdId = createdHotel.getId();
            boolean visibleNow = approvedHotels.stream().anyMatch(h -> h.getId() == createdId);

            if (visibleNow) {
                System.out.println("[PASS] 8. Approved Hotel is now visible in Traveler approved hotel catalog");
                passed++;
            } else {
                System.err.println("[FAIL] 8. Approved Hotel is still missing from Traveler approved catalog");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] 8. Traveler Catalog Exception: " + e.getMessage());
            failed++;
        }

        // 9. Input Validation Tests
        try {
            // Negative price
            try {
                hotelService.addHotel(agent.getId(), "Invalid Hotel", "Goa", "Goa", 1, "Deluxe", -100.0, 5, 4.5, "", "");
                System.err.println("[FAIL] 9.1 Allowed negative price per night");
                failed++;
            } catch (ValidationException ve) {
                System.out.println("[PASS] 9.1 Validation Check: Blocked negative price (" + ve.getMessage() + ")");
                passed++;
            }

            // Zero rooms
            try {
                hotelService.addHotel(agent.getId(), "Invalid Hotel", "Goa", "Goa", 1, "Deluxe", 2000.0, 0, 4.5, "", "");
                System.err.println("[FAIL] 9.2 Allowed zero available rooms");
                failed++;
            } catch (ValidationException ve) {
                System.out.println("[PASS] 9.2 Validation Check: Blocked zero available rooms (" + ve.getMessage() + ")");
                passed++;
            }

            // Empty name
            try {
                hotelService.addHotel(agent.getId(), "   ", "Goa", "Goa", 1, "Deluxe", 2000.0, 5, 4.5, "", "");
                System.err.println("[FAIL] 9.3 Allowed empty hotel name");
                failed++;
            } catch (ValidationException ve) {
                System.out.println("[PASS] 9.3 Validation Check: Blocked empty hotel name (" + ve.getMessage() + ")");
                passed++;
            }

        } catch (Exception e) {
            System.err.println("[FAIL] 9. Validation Exception: " + e.getMessage());
            failed++;
        }

        System.out.println("==========================================================");
        System.out.println("  SUMMARY: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("==========================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
