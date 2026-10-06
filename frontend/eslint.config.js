import jsdoc from 'eslint-plugin-jsdoc';
import tseslint from 'typescript-eslint';

/** ESLint flat config: TypeScript rules, TSDoc on exported symbols, no raw HTML injection. */
export default tseslint.config(
  { ignores: ['dist', 'node_modules', 'coverage', 'playwright-report'] },
  ...tseslint.configs.recommended,
  {
    files: ['src/**/*.{ts,tsx}'],
    plugins: { jsdoc },
    rules: {
      'jsdoc/require-jsdoc': [
        'error',
        {
          publicOnly: true,
          require: {
            FunctionDeclaration: true,
            ClassDeclaration: true,
            ArrowFunctionExpression: false,
            FunctionExpression: false,
          },
          contexts: ['TSInterfaceDeclaration', 'TSTypeAliasDeclaration', 'ExportNamedDeclaration > VariableDeclaration'],
        },
      ],
      'no-restricted-syntax': [
        'error',
        {
          selector: "JSXAttribute[name.name='dangerouslySetInnerHTML']",
          message: 'dangerouslySetInnerHTML is banned: all user text must render as plain text.',
        },
      ],
    },
  },
);
