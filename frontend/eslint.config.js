import eslint from '@eslint/js';
import tseslint from 'typescript-eslint';

export default tseslint.config(
  eslint.configs.recommended,
  ...tseslint.configs.recommended,
  {
    rules: {
      '@typescript-eslint/no-explicit-any': 'warn',
      'max-lines': ['error', { max: 500, skipBlankLines: true, skipComments: true }],
    }
  },
  {
    files: ['src/**/*.ts'],
    rules: {
      // Add custom rules for component size or other project-specific requirements
    }
  }
);
