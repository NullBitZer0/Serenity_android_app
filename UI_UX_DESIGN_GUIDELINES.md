# Serenity App UI/UX Design Guidelines

## 60-30-10 Color Theory Implementation

We've implemented the 60-30-10 color theory to create a harmonious and visually appealing interface:

### Color Palette

1. **60% Dominant Color (Primary)** - Deep Indigo (#283593)
   - Used for primary actions, navigation elements, and key UI components
   - Creates visual hierarchy and brand recognition

2. **30% Supporting Color (Secondary)** - Soft Blue (#6EC6FF)
   - Used for secondary actions, progress indicators, and supporting elements
   - Provides contrast while maintaining harmony with the primary color

3. **10% Accent Color** - Warm Orange (#FFB74D)
   - Used for highlights, important notifications, and key interactive elements
   - Creates visual interest and draws attention to important actions

### Dark Mode Support

The app includes a carefully designed dark mode theme that maintains the 60-30-10 color relationships while ensuring proper contrast and readability.

## Responsive Design

### Dimension Resources

We've implemented a responsive design system using dimension resources:

1. **Default Values** (values/dimens.xml)
   - Optimized for phones and smaller screens
   - Appropriate spacing and sizing for touch interactions

2. **Large Screen Values** (values-sw600dp/dimens.xml)
   - Optimized for tablets and larger screens
   - Increased spacing and sizing for better usability on larger devices

### Key Responsive Elements

- Card layouts with adaptive margins and corner radii
- Flexible text sizing for different screen densities
- Properly spaced interactive elements for touch targets
- Adaptive layout margins and padding

## Typography System

### Text Styles

1. **Headline1** - Large, bold text for main titles
2. **Headline2** - Medium, semi-bold text for section headers
3. **Body1** - Regular text for primary content
4. **Body2** - Light text for secondary content and descriptions

### Font Choices

We've implemented the Poppins font family for a clean, modern appearance:
- Poppins Bold for headlines
- Poppins SemiBold for subheadings
- Poppins Regular for body text
- Poppins Light for secondary text

## Component Design

### Cards

- Rounded corners (16dp default, 18dp on tablets)
- Subtle elevation for depth perception
- Consistent padding and margins
- Surface-appropriate colors for both light and dark modes

### Buttons

- Rounded corners (12dp default, 14dp on tablets)
- Consistent sizing and padding
- Appropriate color usage based on hierarchy
- Clear visual feedback for interactions

### Progress Indicators

- Subtle styling that doesn't overpower content
- Consistent height across different screen sizes
- Color-coded to match the overall theme

## Accessibility

### Color Contrast

All color combinations meet WCAG 2.1 AA standards for contrast:
- Text and background combinations
- Interactive element states
- Icon and graphic visibility

### Touch Targets

All interactive elements meet minimum touch target sizes:
- 48dp minimum for standalone interactive elements
- Appropriate spacing between adjacent controls
- Clear visual boundaries for interactive areas

## Implementation Notes

### Theme Attributes

We use theme attributes extensively to ensure consistency:
- `?attr/colorPrimary` for primary actions
- `?attr/colorSecondary` for secondary elements
- `?attr/colorSurface` for card backgrounds
- `?attr/android:colorBackground` for screen backgrounds

### Dimension References

All layout dimensions use resource references:
- `@dimen/spacing_medium` for standard padding/margins
- `@dimen/card_corner_radius` for consistent card styling
- `@dimen/button_corner_radius` for button styling

This approach ensures consistent styling across the app and makes future design updates easier to implement.