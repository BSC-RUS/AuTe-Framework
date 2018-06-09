# AuTe Framework-wiremock UI

UI for AuTe Framework-wiremock

## Settings

Set up Wiremock's URL in file `src/service/wire-mock.service.ts`:

```
public serviceUrl = 'http://piphagor.bscmsc.ru/bsc-wire-mock';
```

Set up base path URL in `index.html`:

```
<base href="/">
```

## Running application

When WireMock is started all mapping stored on server would be applied. New mapping applies after it's creation. To save mapping on disk click "Save to back storage".

## Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `-prod` flag for a production build.
