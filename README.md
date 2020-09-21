# Architecture

The application is composed of different Microservices:

- API Gateway, which acts as a proxy and manages security by relaying JWTs to the microservices;
- Core Service, which includes the accommodations, bookings & reviews functionality;
- User Service, which connects to Okta API to fetch/edit user information;
- Payment Service, which connects to Paypal Sandbox API and handles payments/refunds;
- Notification Service, which manages streams of user notifications;
- Image Service, which connects to Imgur API and allows hosts to upload pictures of their accommodations;



