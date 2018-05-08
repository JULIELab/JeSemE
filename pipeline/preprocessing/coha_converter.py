from docopt import docopt
import sys

#transforms wlp files into plaintext



def main(args):
    RAW = 0
    LEMMA = 1
    POS = 2

    in_files = args["<wlp_files>"]
    column = LEMMA if args["--lemma"] else RAW
    lower = args["--lower"]

    for in_file in in_files:
        txt = []
        try:
            with open(in_file, "r") as f:
                for line in f:
                    if not line.startswith("//"):
                        parts = line.strip().split("\t")
                        if len(parts) == 3 and parts[RAW] != "@" and not parts[RAW].startswith("@@") and parts[POS] != "null" and not (parts[RAW] == "\x00" and parts[LEMMA] == "\x00") and not parts[RAW].startswith("&"):
                            if column == RAW or parts[LEMMA] == "\x00":
                                it = parts[RAW]
                            else:
                                it = parts[column]
                            if it == "n't":
                                it = "not"
                            if not it == "q":
                                txt.append(it)
                txt = " ".join(txt)
                if lower:
                    txt = txt.lower()
                print(txt)
        except:
            sys.stderr.write("Error in "+in_file)

if __name__ == "__main__":
    args = docopt("""
        Usage:
            coha_converter.py [options] <wlp_files>...

        Options:
            --lemma  Output lemmata instead of tokens
            --lower  Lowercase output
    """)
    main(args)