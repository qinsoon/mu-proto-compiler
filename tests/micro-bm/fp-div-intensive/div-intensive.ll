; ModuleID = 'div-intensive.c'
target datalayout = "e-p:64:64:64-i1:8:8-i8:8:8-i16:16:16-i32:32:32-i64:64:64-f32:32:32-f64:64:64-v64:64:64-v128:128:128-a0:0:64-s0:64:64-f80:128:128-n8:16:32:64-S128"
target triple = "x86_64-apple-macosx10.7.0"

; Function Attrs: nounwind readnone ssp uwtable
define i32 @main() #0 {
entry:
  br label %while.body

while.body:                                       ; preds = %entry, %while.body
  %n.04 = phi i32 [ 0, %entry ], [ %inc, %while.body ]
  %sum.03 = phi double [ 0.000000e+00, %entry ], [ %add, %while.body ]
  %inc = add nsw i32 %n.04, 1
  %conv = sitofp i32 %inc to double
  %div = fdiv double 1.000000e+00, %conv
  %add = fadd double %sum.03, %div
  %cmp = fcmp olt double %add, 2.000000e+01
  br i1 %cmp, label %while.body, label %while.end

while.end:                                        ; preds = %while.body
  ret i32 0
}

attributes #0 = { nounwind readnone ssp uwtable "less-precise-fpmad"="false" "no-frame-pointer-elim"="true" "no-frame-pointer-elim-non-leaf" "no-infs-fp-math"="false" "no-nans-fp-math"="false" "stack-protector-buffer-size"="8" "unsafe-fp-math"="false" "use-soft-float"="false" }

!llvm.ident = !{!0}

!0 = metadata !{metadata !"clang version 3.4 (tags/RELEASE_34/final)"}
