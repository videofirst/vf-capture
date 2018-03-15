import createTheme from 'styled-components-theme';
import colors from './colors';

const theme = createTheme(...Object.keys(colors));

export default theme;