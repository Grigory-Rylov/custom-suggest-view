# Custom suggest view

Proof of concept suggest view with bubble resizing without requesting layout.

Когда делается `Scroller#fling()` с большим `velocity`, в `Scroller` рассчитывается дистанция 
`mDistance` до которой произошел бы доскролл, но для рассчета текущего положения `mCurrX` используется 
значение `mFinalX`, которое ограничивается значением `mMaxX`.