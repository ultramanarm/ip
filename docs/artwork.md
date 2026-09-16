# Glennon GUI artwork

Generated with the built-in image-generation tool for the navy and mint
mission-control theme. These are standalone assets, ready for GUI integration.
No Java, FXML, or CSS behavior changed when adding them.

## Assets

- [Avatar](../src/main/resources/images/glennon-avatar.png): transparent PNG.
  Use as a compact profile picture or header mark, around 36–40 pixels wide.
- [Background](../src/main/resources/images/mission-background.png): portrait
  PNG with quiet central space and subtle orbital details toward the edges.
  Keep message cards opaque to preserve contrast. The existing scroll viewport
  has an opaque fill, so apply this artwork to that viewport when integrating it.

The original PNG files are preserved without resizing or recompression.

## Avatar generation prompt

```text
Use case: stylized-concept
Asset type: square profile picture for Glennon, a compact JavaFX mission-tracking chatbot.
Primary request: create one friendly, distinctive mission-control assistant robot head as a polished raster illustration.
Subject: a softly rounded, compact robot head with a dark navy graphite shell, a pale mint face panel, two simple expressive dark eyes, and small integrated ear-like side panels. Calm, helpful expression. Head only, no body or accessories.
Style/medium: refined soft 3D illustration, smooth matte surfaces, restrained soft highlights, crisp silhouette. Charming but not toy-like or overly detailed.
Composition/framing: square 1024x1024 canvas; centered front three-quarter view; complete head within the central 70 percent, with generous transparent margins safe for circular or rounded-square cropping. The face must remain immediately readable at 32–40 pixels.
Color palette: match the existing app: midnight navy #101d26, slate #172a35, pale mint #a9ddca, restrained off-white highlights.
Background: genuinely transparent alpha, including the margins. Isolated clean cutout.
Constraints: no text, initials, logos, watermark, border, badge, scenery, cast shadow outside the head, or extra objects. Do not simulate transparency with a checkerboard. Deliver a single avatar image.
```

## Background generation prompt

```text
Use case: stylized-concept
Asset type: subtle portrait background artwork for the conversation area of Glennon, a compact mission-tracking chatbot.
Primary request: create a calm, beautiful midnight mission-control background that keeps chat text easy to read.
Scene/backdrop: an abstract deep-space atmosphere, dominated by an almost-solid midnight navy #101d26 field. Extremely faint diffuse teal haze and one or two soft orbital arcs appear only toward the outer edges, with very sparse tiny dim star-like specks near the perimeter. It should feel like a quiet night sky, not a dashboard or a busy galaxy.
Style/medium: premium atmospheric digital painting with smooth gradients and fine, unobtrusive texture, no hard edges.
Composition/framing: portrait approximately 1024x1536; broad uninterrupted negative space across the central 75 percent for dense chat content. Keep decoration low-contrast enough for comfortable reading even when cropped to narrower or wider windows. No focal object in the center.
Color palette: overwhelmingly #101d26 and #14232d, with restrained desaturated slate-teal #233c43 and very faint mint-tinted light. All decoration much darker and less saturated than the app's pale mint #a9ddca accent.
Constraints: opaque image, no text, UI, panels, grids, people, robots, planets, spacecraft, bright stars, bright light sources, logos, watermark, or border. Deliver one finished background texture, not a mockup.
```

