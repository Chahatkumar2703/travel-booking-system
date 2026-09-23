-- ==========================================================
-- Online Travel Booking System Sample Data Seed Script
-- ==========================================================

USE travel_booking_system;

-- 1. Insert Users (admin123 and user123 hashed via SHA-256)
-- admin123: 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
-- user123: e606e38b0d8c19b24cf0ee3808183162ea7cd63ff7912dbb22b5e803286b4446

INSERT INTO users (id, full_name, email, phone, password_hash, role, status) VALUES
(1, 'System Administrator', 'admin@travel.com', '9876543210', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN', 'ACTIVE'),
(2, 'Priya Sharma', 'priya@example.com', '9811223344', 'e606e38b0d8c19b24cf0ee3808183162ea7cd63ff7912dbb22b5e803286b4446', 'USER', 'ACTIVE'),
(3, 'Rahul Verma', 'rahul@example.com', '9822334455', 'e606e38b0d8c19b24cf0ee3808183162ea7cd63ff7912dbb22b5e803286b4446', 'USER', 'ACTIVE'),
(4, 'Amit Patel', 'amit@example.com', '9833445566', 'e606e38b0d8c19b24cf0ee3808183162ea7cd63ff7912dbb22b5e803286b4446', 'USER', 'ACTIVE')
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

-- 2. Insert Destinations
INSERT INTO destinations (id, name, state, description, attractions, best_time) VALUES
(1, 'Goa', 'Goa', 'Tropical paradise famed for pristine beaches, vibrant nightlife, Portuguese heritage, and seafood delicacies.', 'Baga Beach, Calangute, Fort Aguada, Dudhsagar Falls, Basilica of Bom Jesus', 'November to February'),
(2, 'Manali', 'Himachal Pradesh', 'Breathtaking high-altitude Himalayan resort town known for snowy peaks, pine forests, and adventure sports.', 'Solang Valley, Rohtang Pass, Hadimba Temple, Old Manali, Jogini Waterfall', 'October to June'),
(3, 'Jaipur', 'Rajasthan', 'The iconic Pink City celebrated for grand palaces, formidable hill forts, rich royal history, and colorful bazaars.', 'Hawa Mahal, Amer Fort, City Palace, Jantar Mantar, Nahargarh Fort', 'October to March'),
(4, 'Kashmir', 'Jammu & Kashmir', 'Heaven on Earth blessed with postcard-perfect valleys, shikara boat rides on Dal Lake, and snow-laden peaks.', 'Dal Lake Srinagar, Gulmarg Gondola, Pahalgam Valley, Sonamarg, Mughal Gardens', 'March to October (Summer) & Dec-Feb (Snow)'),
(5, 'Delhi', 'Delhi NCR', 'The historical and cultural capital of India, blending centuries of Mughal and colonial monuments with modern charm.', 'Red Fort, Qutub Minar, India Gate, Humayun Tomb, Lotus Temple, Chandni Chowk', 'October to March'),
(6, 'Kerala', 'Kerala', 'Gods Own Country, world-renowned for tranquil backwaters, emerald tea plantations, Ayurveda, and coastal beaches.', 'Alleppey Houseboats, Munnar Tea Gardens, Wayanad Wildlife, Kovalam Beach, Periyar', 'September to March'),
(7, 'Rishikesh', 'Uttarakhand', 'The Yoga Capital of the World along the holy Ganges, known for white-water rafting, serene ashrams, and suspension bridges.', 'Lakshman Jhula, Triveni Ghat Ganga Aarti, Shivpuri Rafting, Beatles Ashram, Neer Waterfall', 'September to April'),
(8, 'Udaipur', 'Rajasthan', 'The romantic City of Lakes known for shimmering waters, opulent marble palaces, and sunset boat rides.', 'City Palace, Lake Pichola, Jag Mandir, Saheliyon Ki Bari, Fatehsagar Lake', 'September to March')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 3. Insert Travel Packages
INSERT INTO packages (id, package_name, destination_id, duration_days, duration_nights, price_per_person, places_covered, hotel_included, food_included, transport_included, description, status) VALUES
(1, 'Goa Sun, Sand & Carnival Tour', 1, 4, 3, 8999.00, 'North Goa Beaches, Fort Aguada, Panaji Cruise, Old Goa Churches', TRUE, TRUE, TRUE, 'Unwind in sunny Goa with beach parties, water sports, sunset cruises, and rich Portuguese architectural tours.', 'ACTIVE'),
(2, 'Manali Adventure & Snow Experience', 2, 5, 4, 12999.00, 'Solang Valley, Atal Tunnel, Rohtang Pass, Old Manali, Vashisht Baths', TRUE, TRUE, TRUE, 'High adrenaline adventure featuring paragliding, snow skiing, river rafting, and scenic mountain cafes.', 'ACTIVE'),
(3, 'Royal Jaipur Heritage & Palaces', 3, 3, 2, 6499.00, 'Amer Fort, Hawa Mahal, City Palace, Chokhi Dhani, Jal Mahal', TRUE, TRUE, TRUE, 'Experience Maharaja hospitality with guided palace tours, traditional Rajasthani dinner, and shopping in historic bazaars.', 'ACTIVE'),
(4, 'Kashmir Valley Paradise Honeymoon Tour', 4, 6, 5, 19999.00, 'Srinagar Dal Lake, Gulmarg, Pahalgam Betaab Valley, Mughal Gardens', TRUE, TRUE, TRUE, 'Romantic getaway with luxury houseboat stay, shikara rides, snow activities in Gulmarg, and saffron valley excursions.', 'ACTIVE'),
(5, 'Delhi Historical & Cultural Trail', 5, 2, 1, 3999.00, 'Qutub Minar, India Gate, Red Fort, Humayun Tomb, Akshardham Temple', TRUE, FALSE, TRUE, 'Explore the imperial monuments, vibrant street markets, and modern landmarks of India capital.', 'ACTIVE'),
(6, 'Kerala Backwaters & Munnar Hills', 6, 5, 4, 15499.00, 'Munnar Tea Plantations, Mattupetty Dam, Alleppey Houseboat, Cochin Fort', TRUE, TRUE, TRUE, 'A blissful blend of misty mountain plantations and an overnight luxury houseboat cruise through tranquil backwater lagoons.', 'ACTIVE'),
(7, 'Rishikesh Rafting & Yoga Retreat', 7, 3, 2, 5999.00, 'Shivpuri 16km White Water Rafting, Cliff Jumping, Ganga Aarti, Camping', TRUE, TRUE, TRUE, 'Rejuvenate your spirit with riverside luxury camping, exhilarating river rafting, bonfire nights, and meditation sessions.', 'ACTIVE'),
(8, 'Udaipur Royal Lakes & Sunset Tour', 8, 4, 3, 11499.00, 'City Palace Udaipur, Lake Pichola Boat Ride, Jag Mandir, Sajjangarh Monsoon Palace', TRUE, TRUE, TRUE, 'Savor royal dining, scenic sunset boat tours, cultural folk dances, and grand Rajput fortress heritage.', 'ACTIVE'),
(9, 'Ultimate Goa Luxury Beachfront Break', 1, 5, 4, 14999.00, 'South Goa Quiet Beaches, Dudhsagar Waterfalls Trek, Spice Plantation, Luxury Cruise', TRUE, TRUE, TRUE, 'Relaxed tropical vacation covering pristine South Goa beaches, waterfalls, and spice plantation organic buffets.', 'ACTIVE'),
(10, 'Himachal Complete Circuit: Manali & Kasol', 2, 6, 5, 16999.00, 'Manali, Solang, Manikaran Hot Springs, Kasol Parvati Valley, Tosh Trek', TRUE, TRUE, TRUE, 'A backpackers dream covering the snow peaks of Manali and the hippie-chic vibes of Parvati Valley.', 'ACTIVE'),
(11, 'Golden Triangle Express (Delhi & Jaipur)', 3, 4, 3, 9999.00, 'Red Fort, Qutub Minar, Amer Fort, Hawa Mahal, Highway Road Trip', TRUE, TRUE, TRUE, 'Seamless intercity package exploring the crown jewels of North Indian heritage with private chauffeur service.', 'ACTIVE'),
(12, 'Wayanad & Kerala Rainforest Expedition', 6, 4, 3, 10999.00, 'Banasura Sagar Dam, Edakkal Caves, Chembra Peak, Bamboo Rafting', TRUE, TRUE, TRUE, 'Offbeat nature excursion into the mist-shrouded Western Ghats rainforests and spice hills.', 'ACTIVE')
ON DUPLICATE KEY UPDATE package_name=VALUES(package_name);

-- 4. Insert Hotels
INSERT INTO hotels (id, hotel_name, destination_id, address, room_type, price_per_night, available_rooms, rating, description, status) VALUES
(1, 'Taj Cidade de Goa Heritage', 1, 'Vainguinim Beach, Panaji, Goa', 'Deluxe Sea View', 4500.00, 15, 4.8, 'Colonial Portuguese luxury resort set right on the golden sands of Vainguinim beach.', 'ACTIVE'),
(2, 'Goa Palms Beach Resort', 1, 'Calangute Beach Road, North Goa', 'Standard AC', 2200.00, 20, 4.2, 'Vibrant boutique hotel within walking distance from famous beach shacks and night markets.', 'ACTIVE'),
(3, 'The Himalayan Spa Resort', 2, 'Hadimba Temple Road, Manali', 'Luxury Mountain Suite', 3800.00, 12, 4.7, 'Picturesque stone-and-wood Himalayan lodge with panoramic views of snow-draped peaks.', 'ACTIVE'),
(4, 'Snow Valley Mountain View Hotel', 2, 'Log Huts Area, Manali', 'Deluxe Room', 2000.00, 18, 4.3, 'Cozy alpine stay offering heated rooms, wooden architecture, and delicious multi-cuisine buffet.', 'ACTIVE'),
(5, 'ITC Rajputana Palace', 3, 'Palace Road, Gopalbari, Jaipur', 'Royal Executive Suite', 4800.00, 10, 4.9, 'Opulent 5-star palace hotel inspired by traditional Rajasthani royal courtyards and havelis.', 'ACTIVE'),
(6, 'Hotel Pearl Palace Heritage', 3, 'Hathroi Fort, Ajmer Road, Jaipur', 'Deluxe Heritage', 1800.00, 16, 4.4, 'Award-winning boutique heritage hotel featuring ornate fresco paintings and a famous rooftop cafe.', 'ACTIVE'),
(7, 'Wangnoo Luxury Houseboats', 4, 'Dal Lake Ghat 12, Srinagar, Kashmir', 'Royal Cedar Suite', 3500.00, 8, 4.8, 'Hand-carved cedar wood houseboat floating on Dal Lake with personalized butler and shikara service.', 'ACTIVE'),
(8, 'The Lalit Grand Palace Srinagar', 4, 'Gupkar Road, Srinagar, Kashmir', 'Palace View Room', 5500.00, 10, 4.9, 'Historic royal palace established by Maharaja Pratap Singh overlooking the serene Dal Lake.', 'ACTIVE'),
(9, 'The Imperial New Delhi', 5, 'Janpath, Connaught Place, New Delhi', 'Heritage Suite', 5000.00, 14, 4.8, 'Iconic British Raj era art-deco luxury hotel located at the center of the capital.', 'ACTIVE'),
(10, 'Lake Song Backwater Resort', 6, 'Vembanad Lake, Kumarakom, Kerala', 'Cottage by Lake', 3400.00, 12, 4.6, 'Traditional Kerala architecture nestled beside Vembanad Lake offering authentic Ayurvedic spa.', 'ACTIVE')
ON DUPLICATE KEY UPDATE hotel_name=VALUES(hotel_name);

-- 5. Insert Sample Bookings
INSERT INTO bookings (id, booking_code, user_id, package_id, hotel_id, travel_date, persons, package_cost, hotel_cost, total_amount, special_requests, booking_status) VALUES
(1, 'TB-2026-1001', 2, 1, 1, DATE_ADD(CURDATE(), INTERVAL 14 DAY), 2, 17998.00, 13500.00, 31498.00, 'Window seat preference for flight and ocean-facing room.', 'CONFIRMED'),
(2, 'TB-2026-1002', 3, 2, 3, DATE_ADD(CURDATE(), INTERVAL 30 DAY), 3, 38997.00, 15200.00, 54197.00, 'Vegetarian meal plan requested for all days.', 'CONFIRMED'),
(3, 'TB-2026-1003', 2, 3, 5, DATE_ADD(CURDATE(), INTERVAL 45 DAY), 2, 12998.00, 9600.00, 22598.00, 'Anniversary decoration requested in room.', 'CONFIRMED'),
(4, 'TB-2026-1004', 4, 7, NULL, DATE_ADD(CURDATE(), INTERVAL 20 DAY), 4, 23996.00, 0.00, 23996.00, 'Need early check-in at riverside camp.', 'CANCELLED')
ON DUPLICATE KEY UPDATE booking_code=VALUES(booking_code);

-- 6. Insert Sample Payments
INSERT INTO payments (id, transaction_code, booking_id, user_id, amount, payment_method, payment_details, payment_status) VALUES
(1, 'TXN-98471011', 1, 2, 31498.00, 'UPI', 'UPI ID: priya@okhdfcbank', 'SUCCESS'),
(2, 'TXN-98471012', 2, 3, 54197.00, 'CARD', 'Credit Card ending with 4242', 'SUCCESS'),
(3, 'TXN-98471013', 3, 2, 22598.00, 'NET_BANKING', 'HDFC Bank NetBanking', 'SUCCESS'),
(4, 'TXN-98471014', 4, 4, 23996.00, 'UPI', 'UPI ID: amit@icici (Refunded)', 'SUCCESS')
ON DUPLICATE KEY UPDATE transaction_code=VALUES(transaction_code);
