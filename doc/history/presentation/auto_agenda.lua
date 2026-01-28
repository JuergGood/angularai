-- auto_agenda.lua
-- Generates an Agenda slide from all level-1 headings except Agenda/Fazit

function Pandoc(doc)
  local agenda_items = {}

  for _, blk in ipairs(doc.blocks) do
    if blk.t == "Header" and blk.level == 1 then
      local title = pandoc.utils.stringify(blk.content)
      if title ~= "Agenda" and title ~= "Fazit" then
        table.insert(agenda_items, title)
      end
    end
  end

  if #agenda_items == 0 then
    return doc
  end

  local bullets = {}
  for _, item in ipairs(agenda_items) do
    table.insert(bullets, pandoc.Plain({pandoc.Str(item)}))
  end

  local agenda_slide = {
    pandoc.Header(1, pandoc.Str("Agenda")),
    pandoc.BulletList(
      (function()
        local list = {}
        for _, item in ipairs(agenda_items) do
          table.insert(list, { pandoc.Plain({ pandoc.Str(item) }) })
        end
        return list
      end)()
    )
  }

  table.insert(doc.blocks, 1, pandoc.Div(agenda_slide))
  return doc
end
