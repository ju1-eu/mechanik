function Image(elem)
  local src = elem.src
  local alt = elem.alt or ""
  local caption = elem.title or ""
  
  local html
  if src:match("%.pdf$") then
    html = string.format([[
<figure class="pdf-container">
  <a href="%s" target="_blank">
    <object data="%s" type="application/pdf" width="100%%" height="600">
      <p>Ihr Browser kann keine eingebetteten PDFs anzeigen. 
      <a href="%s">PDF herunterladen</a>.</p>
    </object>
  </a>
  <figcaption>%s</figcaption>
</figure>
]], src, src, src, caption)
  else
    html = string.format([[
<figure class="image-container">
  <a href="%s" target="_blank">
    <img src="%s" alt="%s" />
  </a>
  <figcaption>%s</figcaption>
</figure>
]], src, src, alt, caption)
  end
  
  return pandoc.RawInline('html', html)
end

function Para(elem)
  if #elem.content == 1 and elem.content[1].t == "Image" then
    return Image(elem.content[1])
  end
end