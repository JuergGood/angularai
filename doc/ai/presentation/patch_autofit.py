#!/usr/bin/env python3
"""patch_autofit.py

Workaround for PowerPoint/Pandoc PPTX where AutoFit appears enabled but isn't applied
until you toggle it in the UI.

This script post-processes a .pptx file and forces text boxes to use "normAutofit"
instead of "noAutofit" (and adds normAutofit where missing) in slide XML.

Usage:
  python patch_autofit.py input.pptx
  python patch_autofit.py input.pptx -o output.pptx
  python patch_autofit.py input.pptx --inplace

Notes:
  - Works by editing ppt/slides/slide*.xml inside the PPTX zip.
  - Applies to shapes that contain a:textBody (regular text boxes/placeholders).
  - Does not touch titles if you pass --skip-titles (recommended if your title should not shrink).
"""

from __future__ import annotations

import argparse
import io
import re
import sys
import zipfile
from pathlib import Path
import xml.etree.ElementTree as ET

NS = {
    "a": "http://schemas.openxmlformats.org/drawingml/2006/main",
    "p": "http://schemas.openxmlformats.org/presentationml/2006/main",
}

for k, v in NS.items():
    ET.register_namespace(k, v)

def _is_title_shape(sp: ET.Element) -> bool:
    """Return True if a shape is a title placeholder."""
    # p:sp/p:nvSpPr/p:nvPr/p:ph@type="title" or "ctrTitle"
    ph = sp.find(".//p:nvPr/p:ph", NS)
    if ph is None:
        return False
    t = (ph.get("type") or "").lower()
    return t in ("title", "ctrtitle")

def _force_norm_autofit(tx_body: ET.Element) -> int:
    """Ensure a:bodyPr contains a:normAutofit (and not a:noAutofit). Return 1 if changed."""
    body_pr = tx_body.find("a:bodyPr", NS)
    if body_pr is None:
        return 0

    changed = 0

    # Ensure wrapping (helps avoid weird overflow)
    if body_pr.get("wrap") in (None, "", "none"):
        body_pr.set("wrap", "square")

    # Remove noAutofit if present
    no = body_pr.find("a:noAutofit", NS)
    if no is not None:
        body_pr.remove(no)
        changed = 1

    # Add normAutofit if missing
    norm = body_pr.find("a:normAutofit", NS)
    if norm is None:
        # Insert early, before any other autofit-ish nodes if possible
        norm = ET.Element(f"{{{NS['a']}}}normAutofit")
        # Optional tuning: allow a bit of line spacing reduction when shrinking
        norm.set("lnSpcReduction", "20000")  # 2%
        norm.set("fontScale", "90000")      # start scaling down from 90%
        body_pr.insert(0, norm)
        changed = 1

    return changed

def patch_pptx(in_path: Path, out_path: Path, inplace: bool, skip_titles: bool) -> int:
    if inplace:
        out_path = in_path

    with zipfile.ZipFile(in_path, "r") as zin:
        # Write a new zip to memory (or to temp) then persist
        buf = io.BytesIO()
        with zipfile.ZipFile(buf, "w", compression=zipfile.ZIP_DEFLATED) as zout:
            changes = 0
            for item in zin.infolist():
                data = zin.read(item.filename)

                if re.match(r"ppt/slides/slide\d+\.xml$", item.filename):
                    try:
                        xml = ET.fromstring(data)
                    except Exception:
                        zout.writestr(item, data)
                        continue

                    slide_changed = 0
                    # Iterate shapes with text bodies
                    for sp in xml.findall(".//p:cSld/p:spTree/p:sp", NS):
                        if skip_titles and _is_title_shape(sp):
                            continue
                        tx = sp.find(".//a:txBody", NS)
                        if tx is None:
                            continue
                        slide_changed += _force_norm_autofit(tx)

                    if slide_changed:
                        changes += slide_changed
                        new_bytes = ET.tostring(xml, encoding="utf-8", xml_declaration=True)
                        zout.writestr(item, new_bytes)
                    else:
                        zout.writestr(item, data)
                else:
                    zout.writestr(item, data)

    buf.seek(0)
    out_path.parent.mkdir(parents=True, exist_ok=True)
    with open(out_path, "wb") as f:
        f.write(buf.getvalue())

    return 0

def main(argv: list[str]) -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("pptx", type=Path, help="Input PPTX file")
    ap.add_argument("-o", "--output", type=Path, default=None, help="Output PPTX file")
    ap.add_argument("--inplace", action="store_true", help="Modify the input file in place")
    ap.add_argument("--skip-titles", action="store_true", help="Do not apply AutoFit to title placeholders")
    args = ap.parse_args(argv)

    if not args.pptx.exists():
        print(f"File not found: {args.pptx}", file=sys.stderr)
        return 2

    if args.output and args.inplace:
        print("Choose either --output or --inplace (not both).", file=sys.stderr)
        return 2

    out_path = args.output or args.pptx.with_name(args.pptx.stem + "_autofit.pptx")
    return patch_pptx(args.pptx, out_path, args.inplace, args.skip_titles)

if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))
