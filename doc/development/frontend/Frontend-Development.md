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
- **E2E Tests**: Playwright for comprehensive end-to-end testing and documentation screenshot automation.

Run unit tests:
```bash
npm test
```

Run Playwright tests:
```bash
npx playwright test
```

## Documentation Screenshots

The documentation screenshots in `doc/user-guide/workflows/assets` are automatically generated using Playwright. This ensures they stay up-to-date with the latest UI.

To regenerate the screenshots:
1. Ensure the application is running (or the Playwright webServer is configured).
2. Run the following command in the `frontend/` directory:
```bash
npx playwright test registration-docs forgot-password-docs --project=no-auth
```

For more details, see [Updating Documentation Screenshots](Updating-Documentation-Screenshots.md).

Run lint:
```bash
npm run lint
```