### Playwright Script for Tasks UX Refactoring

I have set up Playwright in your project and created a script that automatically navigates through the Tasks component and captures screenshots of its various states. These screenshots can be used with ChatGPT (or other Vision-capable AI) to get UX improvement suggestions.

#### 1. ChatGPT Integration in IntelliJ IDEA
Regarding your question about better interaction: **Yes, there is a better option.**

Instead of manual copying and pasting, you can use the **JetBrains AI Assistant** or plugins like **"Continue"** or **"Codeium"** which are integrated directly into IntelliJ. 

However, specifically for **ChatGPT**, the best integrated experience is:
*   **JetBrains AI Assistant**: This is the official solution. It has access to your code context and can help with refactoring directly in the editor.
*   **ChatGPT Plugin (by various authors)**: There are several plugins in the Marketplace that allow you to use your own OpenAI API key.
*   **New "OpenAI" Integration in IntelliJ 2024.x+**: JetBrains has been adding more direct support for custom LLM providers.

**Better Workflow Recommendation:**
Using an integrated tool like **Junie** (which I am) or the **JetBrains AI Assistant** is indeed much faster because we can "see" the code structure directly, making the "visual" screenshot step mostly useful for fine-tuning CSS and layout details that are hard to describe in code alone.

#### 2. Playwright Setup
I have performed the following actions:
*   Installed `@playwright/test` in the `frontend` module.
*   Created `frontend/playwright.config.ts` for configuration.
*   Created `frontend/e2e/tasks-ux.spec.ts` containing the automation logic.

#### 3. How to Run the Script
To capture the screenshots, follow these steps:

1.  **Start your Backend and Frontend** as you normally do (e.g., Spring Boot in IntelliJ and `npm start` in the `frontend` directory).
2.  Open a terminal in the `frontend` directory.
3.  Run the Playwright test:
    ```bash
    npx playwright test e2e/tasks-ux.spec.ts
    ```
4.  The screenshots will be saved in the `frontend/e2e-screenshots` folder:
    *   `tasks-default.png`: General view of the task list.
    *   `tasks-add-form.png`: The "Add/Edit Task" card.
    *   `tasks-bulk-actions.png`: The toolbar that appears when tasks are selected.
    *   `tasks-priority-menu.png`: The dropdown menu for changing priority.
    *   `tasks-compact-view.png`: The list in compact mode.

#### 4. Suggested Refactoring Workflow
Once you have the screenshots:
1.  **Analyze**: Upload the screenshots to ChatGPT and ask: *"Here is my current Task Management UI. Based on these screenshots and modern Material Design principles, what UX improvements do you suggest for better usability and visual hierarchy?"*
2.  **Refactor**: Once you have the suggestions, you can ask me (Junie) to implement them. For example: *"Junie, refactor the `TasksComponent` to use a side-panel for adding tasks instead of the card above the list, as suggested by the UX audit."*
