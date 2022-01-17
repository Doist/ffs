@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

import kotlin.js.*
import org.khronos.webgl.*
import org.w3c.dom.*
import org.w3c.dom.events.*
import org.w3c.dom.parsing.*
import org.w3c.dom.svg.*
import org.w3c.dom.url.*
import org.w3c.fetch.*
import org.w3c.files.*
import org.w3c.notifications.*
import org.w3c.performance.*
import org.w3c.workers.*
import org.w3c.xhr.*
import tsstdlib.Omit

typealias DivProps = Omit<React.DetailedHTMLProps<React.ButtonHTMLAttributes<HTMLDivElement>, HTMLDivElement>, String /* "className" | "children" | "`aria-label`" | "`aria-labelledby`" */>

external fun Modal(__0: DivProps /* DivProps & `T$38` */): JSX.Element

external fun ModalCloseButton(props: Omit<NativeButtonProps /* NativeButtonProps & CommonProps & `T$14` */, String /* "type" | "children" | "variant" | "icon" | "startIcon" | "endIcon" | "disabled" | "loading" | "tabIndex" */> /* Omit<NativeButtonProps /* NativeButtonProps & CommonProps & `T$14` */, String /* "type" | "children" | "variant" | "icon" | "startIcon" | "endIcon" | "disabled" | "loading" | "tabIndex" */> & `T$34` */): JSX.Element

external fun ModalHeader(__0: DivProps /* DivProps & `T$39` */): JSX.Element

external fun ModalBody(__0: DivProps /* DivProps & `T$40` */): JSX.Element

external fun ModalFooter(__0: DivProps /* DivProps & `T$41` */): JSX.Element

external fun ModalActions(__0: DivProps /* DivProps & `T$41` */): JSX.Element