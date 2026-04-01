Engineering Assessment – Billing & Collections Microservice
Celia Courtright

Objective: Assess your ability to model service-oriented architectures and write production-grade APIs for a high-availability, transaction-sensitive domain like insurance billing.
Be prepared to present your System Design (Part 1) and parts of your code (Part 2).

# Part 1: System Design Exercise
Design a microservice that manages policy billing and collections. The system should handle:
- Trigger payment reminders and retry logic
- Record and track payments and delinquency status
- Send payment processing to a 3rd party

# Part 2: Hands-On Coding
Build a simple set of REST APIs within a microservice that simulate a billing engine. These can return canned data for each endpoint.
Requirements:
- Retrieve premium schedule for a policy
- Record a payment attempt and its result (success/failure)
- Return a list of policies that are delinquent
- Trigger a retry action for failed payments

Deliverables:
- Diagram of the system design (powerpoint / pdf)
- Zip file or publicly accessible GitHub repo with source code
- README on how to run and test