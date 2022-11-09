/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbstractLayout, Carousel, Dimension, HtmlComponent, HtmlCompPrefSizeOptions} from '../index';

export class CarouselLayout extends AbstractLayout {
  carousel: Carousel;

  constructor(carousel: Carousel) {
    super();
    this.carousel = carousel;
  }

  override layout($container: JQuery) {
    // recalculate style transformation after layout
    this.carousel.recalcTransformation();

    let filmstripSize = this.carousel.htmlComp.availableSize()
      .subtract(this.carousel.htmlComp.insets())
      .subtract(this.carousel.htmlCompFilmstrip.margins());
    let itemSize = this.carousel.htmlComp.availableSize()
      .subtract(this.carousel.htmlComp.insets())
      .subtract(this.carousel.htmlCompFilmstrip.margins());

    if (this.carousel.statusEnabled && this.carousel.htmlCompStatus) {
      let carouselStatusSize = this.carousel.htmlCompStatus.size().add(this.carousel.htmlCompStatus.margins());

      filmstripSize.height -= carouselStatusSize.height;
      itemSize.height -= carouselStatusSize.height;
    }

    let $carouselItems = this.carousel.$carouselItems;
    filmstripSize.width = $carouselItems.length * filmstripSize.width;
    this.carousel.htmlCompFilmstrip.setSize(filmstripSize);

    $carouselItems.forEach(e => {
      let htmlCarouselItem = HtmlComponent.get(e);
      htmlCarouselItem.setSize(itemSize);
    });
  }

  override preferredLayoutSize($container: JQuery, options?: HtmlCompPrefSizeOptions): Dimension {
    let currentIndex = this.carousel.currentItem;
    let dim = new Dimension(1, 1);
    if (currentIndex < this.carousel.$carouselItems.length && currentIndex >= 0) {
      dim = HtmlComponent.get(this.carousel.$carouselItems[currentIndex]).prefSize();
    }
    dim.height += this.carousel.htmlCompStatus.size().height;
    return dim;
  }
}
