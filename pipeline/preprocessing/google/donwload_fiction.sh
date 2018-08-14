URL1="http://storage.googleapis.com/books/ngrams/books/googlebooks-"
URL2="-all-5gram-20120701-"
URL3=".gz"
language="fiction"
abrv="eng-fiction"


rm -rf $language
mkdir $language
cd $language
mkdir log
for part in 0 1 2 3 4 5 6 7 8 9 _ADJ_ _ADP_ _ADV_ _CONJ_ _DET_ _NOUN_ _NUM_ _PRON_ _PRT_ _VERB_ a_ aa ab ac ad ae af ag ah ai aj ak al am an ao ap aq ar as at au av aw ax ay az b_ ba bb bc bd be bf bg bh bi bj bk bl bm bn bo bp br bs bt bu bv bw bx by bz c_ ca cb cc cd ce cf ch ci cj ck cl cm cn co cp cr cs ct cu cv cw cx cy cz d_ da db dc dd de df dg dh di dj dl dm dn do dp dr ds dt du dv dw dx dy dz e_ ea eb ec ed ee ef eg eh ei ej ek el em en eo ep eq er es et eu ev ew ex ey ez f_ fa fb fc fd fe ff fh fi fj fl fm fn fo fp fr fs ft fu fv fw fx fy g_ ga gb gc gd ge gg gh gi gj gk gl gm gn go gp gr gs gu gv gw gy gz h_ ha hb hc hd he hf hg hh hi hj hk hl hm hn ho hp hq hr hs ht hu hv hw hx hy i_ ia ib ic id ie if ig ih ii ij ik il im in io ip iq ir is it iu iv iw ix iy iz j_ ja jb jc jd je jf jg jh ji jj jk jl jm jn jo jp jr js jt ju jv jw jx jy k_ ka kb kc kd ke kf kg kh ki kj kl km kn ko kp kr ks kt ku kv kw kx ky l_ la lb lc ld le lf lg lh li lj ll lm ln lo lp lr ls lt lu lv lw lx ly lz m_ ma mb mc md me mf mg mh mi mj mk ml mm mn mo mp mq mr ms mt mu mv mw mx my mz n_ na nb nc nd ne nf ng nh ni nj nk nl nm nn no np nq nr ns nt nu nv nw nx ny nz o_ oa ob oc od oe of og oh oi oj ok ol om on oo op or os ot other ou ov ow ox oy oz p_ pa pb pc pd pe pf pg ph pi pj pk pl pm pn po pp pr ps pt pu punctuation pv pw px py q_ qa qc qe qi qj ql qm qn qp qs qt qu qv qw qx qy r_ ra rb rc rd re rf rg rh ri rk rl rm rn ro rp rr rs rt ru rv rw rx ry rz s_ sa sb sc sd se sf sg sh si sj sk sl sm sn so sp sq sr ss st su sv sw sx sy sz t_ ta tb tc td te tf tg th ti tj tk tl tm tn to tp tq tr ts tt tu tv tw tx ty tz u_ ua ub uc ud ue uf ug uh ui uj uk ul um un uo up uq ur us ut uu uv uw ux uy uz v_ va vb vc vd ve vf vh vi vj vk vl vm vn vo vp vr vs vt vu vv vw vx vy vz w_ wa wb wc wd we wf wg wh wi wj wk wl wm wn wo wp wr ws wt wu wv ww wx wy wz x_ xa xc xe xh xi xl xm xn xo xs xu xv xx xy y_ ya yb yc yd ye yf yg yh yi yk yl ym yn yo yp yr ys yt yu yv yw yx yz z_ za zb zc zd ze zf zg zh zi zj zk zl zm zn zo zp zr zs zt zu zv zw zx zy zz
do
	wget -c -o log/"$part" "$URL1$abrv$URL2$part$URL3"
done
grep -L "100%" log/* >> ../errors
