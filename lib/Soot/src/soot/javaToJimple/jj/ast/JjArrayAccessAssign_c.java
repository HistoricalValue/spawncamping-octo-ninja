/* Soot - a J*va Optimization Framework
 * Copyright (C) 2004 Jennifer Lhotak
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package soot.javaToJimple.jj.ast;

import polyglot.ast.*;
import polyglot.visit.*;
import polyglot.ext.jl.ast.*;
import polyglot.types.*;
import polyglot.util.*;

public class JjArrayAccessAssign_c extends ArrayAccessAssign_c {

    public JjArrayAccessAssign_c(Position pos, ArrayAccess left, Operator op, Expr right){
        super(pos, left, op, right);
    }

    public Type childExpectedType(Expr child, AscriptionVisitor av){
      if (op == SHL_ASSIGN || op == SHR_ASSIGN || op == USHR_ASSIGN) {
          return child.type();
      }

      if (child == right) {
          return left.type();
      }

      return child.type();


    }
}
