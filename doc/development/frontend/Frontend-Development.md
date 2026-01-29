# Frontend Development

The frontend is a modern Angular application built with Angular 21.

## Key Technologies

- **Standalone Components**: Modular and reusable component architecture.
- **Signals**: Reactive state management using Angular Signals.
- **Angular Material**: UI components following Material Design principles.
- **Modern Control Flow**: Using `@if`, `@for`, and `@empty`.

## Development Setup

To run the frontend locally:
1. Navigate to the `frontend/` directory.
2. Run `npm install`.
3. Run `npm start`.
4. The application will be available at `http://localhost:4200` (or `http://localhost` if using the proxy).

## Testing

- **Unit Tests**: Vitest for component and service testing.
- **E2E Tests**: Cypress for comprehensive end-to-end testing.

Run unit tests:
```bash
npm test
```

Run Cypress tests:
```bash
npm run cypress:open
```

Run lint:
```bash
npm run lint
```