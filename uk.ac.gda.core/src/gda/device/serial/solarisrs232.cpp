/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

#include <device/SOLARISrs232.h>
#include <util/etype.h>

using device::SOLARISrs232;

using device::DeviceException;
using device::BaudRate;
using device::ByteSize;
using device::Parity;
using device::StopBits;


void SOLARISrs232::init (const std::string& name, BaudRate baudRate,
		ByteSize byteSize, Parity parity,
		StopBits stopBits) throw (DeviceException)
{
	_sName = name;
	 // open the device for read/write ...
	if ( (_fd = open( _sName.c_str(), O_RDWR )) == -1 )
	{
		std::string str = "SOLARISrs232 Error opening port: ";
		str += _sName;
		DeviceException e( str.c_str() );
		throw e;
	}

	 // get the attribute structure for this serial port ...
	if( (tcgetattr( _fd, &_term_attr )) == -1 ) {
		std::string str = "SOLARISrs232 Error getting port attributes: ";
		str += _sName;
		DeviceException e( str.c_str() );
		throw e;
	}

	/* set the flags to represent 'raw' mode
	 * (see man page termio(7) for flags descriptions) ...
	 */
	_term_attr.c_iflag &= ~(INLCR|ICRNL|IUCLC|ISTRIP|IXON|BRKINT);
	_term_attr.c_oflag &= ~OPOST;
	_term_attr.c_lflag &= ~(ICANON|ISIG|ECHO);
	/*
	 * block until a character is read, or until an initial
	 * timeout of half a second ...
	 */
	_term_attr.c_cc[VMIN] = 0;
	_term_attr.c_cc[VTIME] = 1;

	if ( tcsetattr(_fd, TCSANOW, &_term_attr) == -1 )
	{
		std::string str = "SOLARISrs232 Error setting port defaults: ";
		str += _sName;
		DeviceException e( str.c_str() );
		throw e;
	}

	 // set the baud rate
	setBaudRate( baudRate );
	 // set the byte size
	setByteSize( byteSize );
	 // set the parity
	setParity( parity );
	 // set the stop bits
	setStopBits( stopBits );
}

SOLARISrs232::~SOLARISrs232()
{
	if ( _fd != NULL ) {
		close(_fd);
	}
}

void SOLARISrs232::setBaudRate (BaudRate baudRate)
		throw (DeviceException)
{
	speed_t baud;

	switch( baudRate )
	{
	case BAUDRATE_0:
		baud = B0;
		break;
	case BAUDRATE_50:
		baud = B50;
		break;
	case BAUDRATE_75:
		baud = B75;
		break;
	case BAUDRATE_110:
		baud = B110;
		break;
	case BAUDRATE_134:
		baud = B134;
		break;
	case BAUDRATE_150:
		baud = B150;
		break;
	case BAUDRATE_200:
		baud = B200;
		break;
	case BAUDRATE_300:
		baud = B300;
		break;
	case BAUDRATE_600:
		baud = B600;
		break;
	case BAUDRATE_1200:
		baud = B1200;
		break;
	case BAUDRATE_1800:
		baud = B1800;
		break;
	case BAUDRATE_2400:
		baud = B2400;
		break;
	case BAUDRATE_4800:
		baud = B4800;
		break;
	case BAUDRATE_9600:
		baud = B9600;
		break;
	case BAUDRATE_19200:
		baud = B19200;
		break;
	case BAUDRATE_38400:
		baud = B38400;
		break;
	default:
		 // unsupported baud rate ...
		std::string str = "SOLARISrs232 Unsupported baud rate setting: ";
		str += _sName;
		DeviceException e( str.c_str() );
		throw e;
	}

	tcgetattr( _fd, &_term_attr );

	cfsetispeed( &_term_attr, baud );
	cfsetospeed( &_term_attr, baud );

	/* set the new baud rate after current output has
	 * been transmitted ...
	 */
	if ( tcsetattr(_fd, TCSADRAIN, &_term_attr) == -1 )
	{
		std::string str = "SOLARISrs232 Error setting baud rate: ";
		str += _sName;
		DeviceException e( str.c_str() );
		throw e;
	}
}

void SOLARISrs232::setByteSize (ByteSize byteSize)
		throw (DeviceException)
{
	 // get the attribute structure ...
	tcgetattr( _fd, &_term_attr );

	/* Use <termios.h> defined bit masks to get at
	 * the size bits in c_cflag ...
	 */
	switch( byteSize )
	{
	case BYTESIZE_5:
		_term_attr.c_cflag &= ~CSIZE;
		_term_attr.c_cflag |= CS5;
		break;
	case BYTESIZE_6:
		_term_attr.c_cflag &= ~CSIZE;
		_term_attr.c_cflag |= CS6;
		break;
	case BYTESIZE_7:
		_term_attr.c_cflag &= ~CSIZE;
		_term_attr.c_cflag |= CS7;
		break;
	case BYTESIZE_8:
		_term_attr.c_cflag &= ~CSIZE;
		_term_attr.c_cflag |= CS8;
		break;
	default:
		 // Bad size given ( >8 || <5 ) ...
		std::string str = "SOLARISrs232 Invalid byte size: ";
		str += _sName;
		DeviceException e( str.c_str() );
		throw e;
	}

	if ( tcsetattr( _fd, TCSADRAIN, &_term_attr ) == -1 )
	{
		std::string str = "SOLARISrs232 Error setting byte size: ";
		str += _sName;
		DeviceException e( str.c_str() );
		throw e;
	}
}

void SOLARISrs232::setParity (Parity parity)
		throw (DeviceException)
{
	 // get the attribute structure ...
	tcgetattr( _fd, &_term_attr );

	switch (parity)
	{
	case NO_PARITY:
		_term_attr.c_cflag &= ~PARENB;
		break;
	case ODD_PARITY:
		 // switch on parity and make odd ...
		_term_attr.c_cflag |= PARENB;
		_term_attr.c_cflag |= PARODD;
		break;
	case EVEN_PARITY:
		 // switch on parity and make even ...
		_term_attr.c_cflag |= PARENB;
		_term_attr.c_cflag &= ~PARODD;
		break;
	default:
		 // bad parity type ...
		std::string str = "SOLARISrs232 Unsupported Parity: ";
		str += _sName;
		DeviceException e( str.c_str() );
		throw e;
	}

	if ( tcsetattr( _fd, TCSADRAIN, &_term_attr ) == -1 )
	{
		std::string str = "SOLARISrs232 Error setting parity: ";
		str += _sName;
		DeviceException e( str.c_str() );
		throw e;
	}
}

void SOLARISrs232::setStopBits (StopBits stopBits)
		throw (DeviceException)
{
	 // get the attribute structure ...
	tcgetattr( _fd, &_term_attr );

	switch ( stopBits )
	{
	case ONE_STOPBIT:
		_term_attr.c_cflag &= ~CSTOPB;
		break;
	case TWO_STOPBITS:
		_term_attr.c_cflag |= CSTOPB;
		break;
	default:
		 // bad stopbits value ...
		std::string str = "SOLARISrs232 Invalid stop bits value: ";
		str += _sName;
		DeviceException e( str.c_str() );
		throw e;
   }

	if ( tcsetattr( _fd, TCSADRAIN, &_term_attr ) == -1 )
	{
		std::string str = "SOLARISrs232 Error setting stop bits: ";
		str += _sName;
		DeviceException e( str.c_str() );
		throw e;
	}
}

void SOLARISrs232::setReadTimeout( long time )
		throw (DeviceException)
{
	 // get the attribute structure ...
	tcgetattr( _fd, &_term_attr );

	/* The time parameter to this interface method is given in
	 * milliseconds, but our timer uses .1s granularity ...
	 */
	_term_attr.c_cc[VTIME] = (unsigned char)( time/100 );

	if ( tcsetattr( _fd, TCSADRAIN, &_term_attr ) == -1 )
	{
		std::string str = "SOLARISrs232 Error setting read time-out: ";
		str += _sName;
		DeviceException e( str.c_str() );
		throw e;
	}
}

long SOLARISrs232::getReadTimeout () const
{
	return (long)( _term_attr.c_cc[VTIME] * 100 );
}

char SOLARISrs232::readChar() throw (DeviceException)
{
	char c;
	int retval;

	if ( (retval = read( _fd, &c, 1 )) == -1 )
	{
		 // read error ...
		std::string str = "SOLARISrs232 Read error: ";
		str += _sName;
		DeviceException e( str.c_str() );
		throw e;
	}
	else if ( retval == 0 )
	{
		 // timeout or EOF ...
		std::string str = "SOLARISrs232 Read timeout or error: ";
		str += _sName;
		DeviceException e( str.c_str() );
      throw e;
   }
   return c;
}

void SOLARISrs232::writeChar( char c ) throw (DeviceException)
{
	if ( ( write( _fd, &c, 1 )) == -1 )
	{
		std::string str = "SOLARISrs232 Write error: ";
		str += _sName;
		DeviceException e( str.c_str() );
		throw e;
	}
}

void SOLARISrs232::flush() throw (DeviceException)
{
	int numchars;

	numchars = ioctl( _fd, FIONREAD );

	try
	{
		while( numchars > 0 ) {
			readChar();
		}
	}
	catch( DeviceException& e )
	{
		std::string str = "SOLARISrs232 Flush error: ";
		str += e.getString();
		DeviceException de( str.c_str() );
		throw de;
	}
}



// === END =================================================




