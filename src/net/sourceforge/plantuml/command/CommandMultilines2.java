/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2017, Arnaud Roques
 *
 * Project Info:  http://plantuml.com
 * 
 * If you like this project or if you find it useful, you can support us at:
 * 
 * http://plantuml.com/patreon (only 1$ per month!)
 * http://plantuml.com/paypal
 * 
 * This file is part of PlantUML.
 *
 * PlantUML is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PlantUML distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 *
 * Original Author:  Arnaud Roques
 * 
 *
 */
package net.sourceforge.plantuml.command;

import net.sourceforge.plantuml.StringUtils;
import net.sourceforge.plantuml.command.regex.Matcher2;
import net.sourceforge.plantuml.command.regex.MyPattern;
import net.sourceforge.plantuml.command.regex.RegexConcat;
import net.sourceforge.plantuml.core.Diagram;

public abstract class CommandMultilines2<S extends Diagram> implements Command<S> {

	private final RegexConcat starting;

	private final MultilinesStrategy strategy;

	public CommandMultilines2(RegexConcat patternStart, MultilinesStrategy strategy) {
		if (patternStart.getPattern().startsWith("^") == false || patternStart.getPattern().endsWith("$") == false) {
			throw new IllegalArgumentException("Bad pattern " + patternStart.getPattern());
		}
		this.strategy = strategy;
		this.starting = patternStart;
	}

	public boolean syntaxWithFinalBracket() {
		return false;
	}

	public abstract String getPatternEnd();

	public String[] getDescription() {
		return new String[] { "START: " + starting.getPattern(), "END: " + getPatternEnd() };
	}

	final public CommandControl isValid(BlocLines lines) {
		lines = lines.cleanList2(strategy);
		if (isCommandForbidden()) {
			return CommandControl.NOT_OK;
		}
		if (syntaxWithFinalBracket()) {
			if (lines.size() == 1 && StringUtils.trin(lines.getFirst499()).endsWith("{") == false) {
				final String vline = lines.get499(0).toString() + " {";
				if (isValid(BlocLines.single(vline)) == CommandControl.OK_PARTIAL) {
					return CommandControl.OK_PARTIAL;
				}
				return CommandControl.NOT_OK;
			}
			lines = lines.eventuallyMoveBracket();
		}
		final CharSequence first = lines.getFirst499();
		if (first == null) {
			return CommandControl.NOT_OK;
		}
		final boolean result1 = starting.match(StringUtils.trin(first));
		if (result1 == false) {
			return CommandControl.NOT_OK;
		}
		if (lines.size() == 1) {
			return CommandControl.OK_PARTIAL;
		}

		final Matcher2 m1 = MyPattern.cmpile(getPatternEnd()).matcher(StringUtils.trinNoTrace(lines.getLast499()));
		if (m1.matches() == false) {
			return CommandControl.OK_PARTIAL;
		}

		actionIfCommandValid();
		return CommandControl.OK;
	}

	public final CommandExecutionResult execute(S system, BlocLines lines) {
		lines = lines.cleanList2(strategy);
		if (syntaxWithFinalBracket()) {
			lines = lines.eventuallyMoveBracket();
		}
		return executeNow(system, lines);
	}

	protected abstract CommandExecutionResult executeNow(S system, BlocLines lines);

	protected boolean isCommandForbidden() {
		return false;
	}

	protected void actionIfCommandValid() {
	}

	protected final RegexConcat getStartingPattern() {
		return starting;
	}

}
