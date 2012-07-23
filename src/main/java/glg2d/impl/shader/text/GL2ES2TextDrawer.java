/*
 * Copyright 2012 Brandon Borkholder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package glg2d.impl.shader.text;

import static glg2d.impl.AbstractShapeHelper.visitShape;
import glg2d.GLGraphics2D;
import glg2d.impl.AbstractTextDrawer;
import glg2d.impl.shader.GLShaderGraphics2D;
import glg2d.impl.shader.text.CollectingTesselator.TesselatedTriangles;

import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import javax.media.opengl.GL2ES2;

public class GL2ES2TextDrawer extends AbstractTextDrawer {
  protected GLShaderGraphics2D g2d;
  protected GL2ES2 gl;

  protected TextPipeline pipeline;

  public GL2ES2TextDrawer() {
    this(new TextPipeline());
  }

  public GL2ES2TextDrawer(TextPipeline pipeline) {
    this.pipeline = pipeline;
  }

  @Override
  public void setG2D(GLGraphics2D g2d) {
    if (g2d instanceof GLShaderGraphics2D) {
      this.g2d = (GLShaderGraphics2D) g2d;
    } else {
      throw new IllegalArgumentException(GLGraphics2D.class.getName() + " implementation must be instance of "
          + GLShaderGraphics2D.class.getSimpleName());
    }

    gl = g2d.getGLContext().getGL().getGL2ES2();
    if (!pipeline.isSetup()) {
      pipeline.setup(gl);
    }

    super.setG2D(g2d);
  }

  @Override
  public void dispose() {
    pipeline.delete(gl);
  }

  @Override
  public void drawString(String string, int x, int y) {
    drawString(string, (float) x, (float) y);
  }

  @Override
  public void drawString(AttributedCharacterIterator iterator, int x, int y) {
    drawString(iterator, (float) x, (float) y);
  }

  @Override
  public void drawString(AttributedCharacterIterator iterator, float x, float y) {
    drawChars(iterator, x, y);
  }

  @Override
  public void drawString(String string, float x, float y) {
    drawChars(new StringCharacterIterator(string), x, y);
  }

  protected void drawChars(CharacterIterator textItr, float x, float y) {
    GlyphVector glyphs = getFont().createGlyphVector(getFontRenderContext(), textItr);

    pipeline.use(gl, true);
    pipeline.setColor(gl, g2d.getUniformsObject().colorHook.getRGBA());
    pipeline.setTransform(gl, g2d.getUniformsObject().transformHook.getGLMatrixData());

    for (int i = 0; i < 1; i++) {
      Shape s = glyphs.getOutline();

      CollectingTesselator tess = new CollectingTesselator();
      visitShape(s, tess);
      TesselatedTriangles triangles = tess.getTesselated();

      pipeline.setLocation(gl, x, y + i * 10);

      pipeline.bindBuffer(gl);
      triangles.draw(gl);
      pipeline.unbindBuffer(gl);
    }

    pipeline.use(gl, false);
  }
}